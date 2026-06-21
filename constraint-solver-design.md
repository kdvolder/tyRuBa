# Constraint Solver Design

## The Problem with TypeEnv

Currently, `TypeEnv` maps `RBVariable → Type`. Each entry `?x → Integer` means: `?x = Integer` (equality constraint).

But we need to record:
- `?x <: Integer` (lower bound)
- `?x :> Integer` (upper bound)
- `?x = Integer` (exact type)

A single `Type` can't represent both `<:` and `:>` constraints. We need a **constraint store** that separates these.

## The Solution: ConstraintSolver

Replace `TypeEnv` with a `ConstraintSolver` object that:
1. Has an API to 'add' constraints
2. Internally tracks what is still possible (the lattice of types)
3. Throws `TypeModeError` immediately if a constraint makes the solution set empty (unsatisfiable)
4. At the end, provides the solution (the narrowed types for each variable)

### Two Kinds of Constraints

The `ConstraintSolver` tracks two kinds of constraints:

1. **Type constraints**: `?a <: Integer`, `?a :> Number`, `?a = Integer`
2. **Equality constraints**: `?a = ?b` (unification)

When intersecting two solvers:
- Merge equality groups (using union-find)
- For each equality group, intersect the type constraints of all members

Example:
- Solver A: `{?a = ?b, ?a <: Integer}`
- Solver B: `{?a = ?c, ?c <: String}`
- Intersect: `{?a = ?b = ?c, ?a <: Integer, ?a <: String}`
- Solve: `Integer.intersect(String)` = `Nothing` → contradiction

### Proposed API

```java
public class ConstraintSolver {
    // Initialize with existing constraints (from outer scopes)
    public ConstraintSolver(TypeEnv initialEnv);
    
    // Add a constraint: ?x <: T
    public void addLowerBound(RBVariable x, Type T);
    
    // Add a constraint: ?x :> T
    public void addUpperBound(RBVariable x, Type T);
    
    // Add a constraint: ?x = T
    public void addEquality(RBVariable x, Type T);
    
    // Add a constraint: ?x = ?y (equality between variables)
    public void addVariableEquality(RBVariable x, RBVariable y);
    
    // Check if current constraints are satisfiable
    // Throws TypeModeError if not
    public void checkSatisfiable();
    
    // Solve and return the narrowed types
    // Returns a TypeEnv with concrete types for all variables
    public TypeEnv solve();
    
    // Lattice operations on the solution space
    // intersect: Merge constraints (conjunction). If S1 has constraints {?x <: A} 
    // and S2 has {?x <: B}, then S1.intersect(S2) has {?x <: A, ?x <: B}.
    // Merges equality groups and intersects type constraints within each group.
    public ConstraintSolver intersect(ConstraintSolver other);
    
    // union: Merge solution sets (disjunction). If S1 has solutions ?x = A 
    // and S2 has solutions ?x = B, then S1.union(S2) has solutions ?x = A|B.
    public ConstraintSolver union(ConstraintSolver other);
}

### Internal Data Structure

The `ConstraintSolver` uses:
- **Union-find** for equality constraints (`?a = ?b`)
- **Type constraints per equality group** (not per variable)
- **Lattice operations** on type constraints within each group

When solving:
1. Resolve each equality group to a common type (LUB of all types in the group)
2. Return a `TypeEnv` with the common type for all variables in the group
```

### How it fits into `typecheck()`

Current signature:
```java
TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv)
```

New signature:
```java
TypeEnv typecheck(PredInfoProvider predinfo, ConstraintSolver solver)
```

Inside `typecheck()`:
```java
// Instead of:
// Type intersection = argType.intersect(predType[i]);

// We do:
solver.addLowerBound(var, argType);
solver.addUpperBound(var, predType[i]);
solver.checkSatisfiable(); // Throws if unsatisfiable
```

At the end of `typecheck()`:
```java
return solver.solve();
```

## Lattice Requirements

For the `ConstraintSolver` to work, types must form a **lattice**:

1. **Partial order**: `<:` relation (subtype) ✅ Exists (`isSubTypeOf`)
2. **Least Upper Bound (LUB)**: For any two types, their LUB exists ✅ Partially exists (`union()`)
3. **Greatest Lower Bound (GLB)**: For any two types, their GLB exists ❌ Doesn't work for composite types (`intersect()`)

The `ConstraintSolver` relies on `Type.union()` (LUB) and `Type.intersect()` (GLB) to compute narrowed types. These methods exist in the API but are broken for composite types (lists, tuples). Fixing them is a prerequisite for the constraint solver.

## Disjunctions (Broadening Constraints)

Disjunctions (`;`) **broaden** constraints. In lattice terms, they take the **union** of possibilities across branches.

The `ConstraintSolver` exposes a `union()` method to handle this:
1. Clone the solver for each branch
2. Solve each branch independently
3. Merge results using `solverA.union(solverB)`

Example:
```tyruba
thing(?x) :- Integer(?x) ; String(?x).
```
- Branch 1: `?x <: Integer` → narrowed to `Integer`
- Branch 2: `?x <: String` → narrowed to `String`
- Merge: `solverA.union(solverB)` → `?x = Integer|String`

The `ConstraintSolver.union()` method handles the merging by iterating over variables and computing `T1.union(T2)` for each. Variables present in only one branch are handled naturally (their type is just `T1.union(Any)` or similar, depending on semantics).

## Conjunctions (Narrowing Constraints)

Conjunctions (`,`) **narrow** constraints. In lattice terms, they take the **intersection** of possibilities.

The `ConstraintSolver` exposes an `intersect()` method to handle this:
1. Create a new solver
2. Add all constraints from both solvers to the new one
3. Check satisfiability

Example:
```tyruba
thing(?x) :- Integer(?x), ?x = "hello".
```
- `?x <: Integer` and `?x = "hello"` → `?x = "hello"` (if "hello" is an Integer)
- If `?x <: Integer` and `?x = "hello"` (String) → `intersect` fails (unsatisfiable)

The `ConstraintSolver.intersect()` method handles the merging by adding all constraints and checking satisfiability.

## Integration with Existing Code

The `typecheck()` method walks the AST and calls `solver.addLowerBound()` / `solver.addUpperBound()` for each constraint. The `ConstraintSolver` tracks all constraints and checks satisfiability at each step. At the end, `solver.solve()` returns the narrowed `TypeEnv`.

The rest of the code (`prepareForRunning()`, `convertToMode()`, etc.) doesn't need to change. It just calls `typecheck()` and gets back a `TypeEnv`.

## Summary

- **Replace** `TypeEnv` parameter with `ConstraintSolver`
- **API**: `addLowerBound()`, `addUpperBound()`, `addEquality()`, `checkSatisfiable()`, `solve()`
- **Lattice**: Types must support `union()` (LUB) and `intersect()` (GLB) for all types
- **Integration**: `typecheck()` calls solver methods instead of `Type.intersect()`
- **Downstream**: No changes needed outside `typecheck()`

# TyRuBa Type Checker: Black Box Analysis

## Current Type Checking Flow

```
prepareForRunning()
  ├── typecheck(PredInfoProvider, TypeEnv) → TypeEnv
  │   └── walks AST
  │       ├── RBPredicateExpression.typecheck()
  │       │   ├── for each arg: argType.intersect(predType[i])
  │       │   │   └── checkEqualTypes() → HM unification
  │       │   └── returns TypeEnv (merged)
  │       └── RBConjunction.typecheck()
  │           └── iterates subexpressions, intersects TypeEnvs
  └── convertToMode(ModeCheckContext) → RBExpression
```

## The Black Box Contract

**Input:**
- `RBExpression` (AST of query/fact/rule)
- `PredInfoProvider` (predicate declarations with types and modes)
- `TypeEnv` (starting state, usually empty)

**Output:**
- `TypeEnv` (final state with narrowed types) or throws `TypeModeError`

**Side effects:**
- None (pure function)

## Key Types

### `TypeEnv`
A map from `RBVariable` → `Type`. Tracks what types each variable has been assigned during type checking.

```java
public class TypeEnv extends Hashtable {
    Type get(RBSubstitutable v);
    Type basicGet(RBSubstitutable v);
    TypeEnv intersect(TypeEnv other);
    TypeEnv union(TypeEnv other);
}
```

### `Type.intersect()`
Currently does HM-style equality unification via `checkEqualTypes()`.
- If types unify → returns the unified type
- If types don't unify → throws `TypeModeError`

### `PredInfoProvider`
Provides access to predicate declarations:
```java
PredInfo getPredInfo(PredicateIdentifier pred);
```
Each `PredInfo` contains:
- `TupleType getTypeList()` — the declared types of arguments
- `BindingList getBindingList()` — the declared modes

## Downstream Usage

`prepareForRunning()` uses the `TypeEnv` for:
1. Debug output (`System.err.println("inferred types: " + resultEnv)`)
2. Passed into `PreparedQuery` but mostly just printed

**It's not used for anything else.** The mode checking (`convertToMode()`) is completely separate and doesn't use `TypeEnv`.

## Replacing the Black Box

We replace `TypeEnv` with `ConstraintSolver`.

**New signature:**
```java
TypeEnv typecheck(PredInfoProvider predinfo, ConstraintSolver solver)
```

**Pass it in, get it back out:**
```java
// Conjunction (RBConjunction.typecheck)
solver = solver.addConstraint(...);
solver = left.typecheck(predinfo, solver);  // solver comes back changed
solver = right.typecheck(predinfo, solver); // solver comes back changed
return solver.solve();  // only at the top level
```

**`solve()` is a terminal operation.** Call it exactly once, from the caller of the top-level `typecheck()`. Never during traversal. This ensures we never lose constraints by prematurely collapsing the constraint system into a single solution.

**What doesn't change:**
- `RBExpression.typecheck()` still returns `TypeEnv`
- `prepareForRunning()` doesn't need to change (it just calls `typecheck()` and gets back a `TypeEnv`)
- The rest of the code doesn't need to know

## API Changes Needed

1. **Replace `TypeEnv` with `ConstraintSolver`**
   - `ConstraintSolver` accumulates constraints (type bounds + equalities)
   - `ConstraintSolver.solve()` returns a `TypeEnv` at the end

2. **`Type.intersect(Type other)` and `Type.checkEqualTypes()`**
   - No longer called from `typecheck()`
   - May still be needed for `ConstraintSolver.union()` / `intersect()` internally

Everything else stays the same. The rest of the code calls `typecheck()` and gets back a `TypeEnv`. It doesn't care how that `TypeEnv` was computed.

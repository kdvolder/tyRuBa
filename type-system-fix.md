# TyRuBa Type System: A Path Forward

## 1. What Are Types in TyRuBa?

Types in TyRuBa are **descriptive**. They are provable assertions about a program's behavior. They don't change the behavior — they describe it.

Removing types would break parsing and compilation, but the **semantics** of the program (the facts, rules, and truth) is independent of types. Types are annotations on top of a program that is already well-defined without them.

This is different from languages like Java, where types are inseparable from execution. In TyRuBa, the program is just facts and rules. Types are metadata that help the type checker verify consistency. They say things but don't do things.

**The circle metaphor:** Imagine all possible values as squares. Blue squares = values where the predicate is true. Red squares = values where it's false. The type is the **smallest circle** you can draw around all the blue squares.

- `human :: String` means: "The only tuples where `human` is true are those where the argument falls within the String circle."
- The **type checker** rejects calls where the argument type has an **empty intersection** with the declared type.
- It does NOT guarantee the call will succeed — only that it *could* succeed.

**Key principles:**
1. **Descriptive, not prescriptive**: Types describe what *could* be true, not what *must* be true.
2. **Conservative filter**: The type checker rejects only what's provably inconsistent with the predicate's domain, leaves everything else to the logic engine.
3. **Order-independent**: Must guarantee correctness regardless of how the mode system rearranges the query.

### Current Type Language
- **Atomic types**: `Integer`, `String`, `Number`, etc. (Java class types)
- **Composite types**: Lists `[*]<T>`, tuples `<?t1, ?t2>`, unions `Integer|String`
- **Type variables**: `?t`, `?x`, etc. (polymorphic, can grow/shrink)
- **Subtyping**: `Integer <: Number` (Java class hierarchy)

## 2. What Does a Type Declaration Mean?

### The Bounding Set View
A declaration `p :: A, B` means: "The only tuples for which `p` can be true are those where the arguments fall within the domains A and B."

### The Type Variable View
A declaration `append :: [?t], [?t], [?t]` is **universally quantified**:

> "For any type `?t`, the predicate works."

The type variable `?t` is a **placeholder** for "some type." The declaration makes a **promise** to the type checker: "The predicate's domain includes tuples where all three arguments are lists of *any* type `?t` you choose."

This means the **type checker** is free to choose `?t` when verifying a call. Given `append([1,2,3], ["hello"], ?Result)`, the type checker's job is to find a `?t` that makes this call fall within the predicate's domain. The declaration's universal quantifier is the **promise that makes this possible** — because the predicate works for *any* `?t`, the type checker can pick whatever `?t` works for this call.

### The Domain View
For `append`, the type defines a **3D domain of valid tuples**. The **type checker** rejects calls it can prove **never have results** — because the arguments fall outside the predicate's domain.

For example, `odd("abc")` is rejected when `odd :: Integer`. The type checker can prove `"abc"` (a String) is outside the domain of `odd` (which only accepts Integers). This call has no results — it's provably inconsistent with the predicate's domain.

This is not about "blowing up" or crashing. `odd("abc")` would just return no results. But it's **meaningless** to ask — you're guaranteed 0 results, so the query is logically false. The type checker catches this before the engine even starts searching, saving work and signaling to the programmer that the query is fundamentally unsound.

## 3. The Current System's Problem

The current type checker is based on **Hindley-Milner unification**:
- Efficient, decidable, simple.
- Only handles equality constraints (`=`).
- Solves by immediate binding: if you see `?t = Integer`, bind `?t` to `Integer` right away.

**The "growable type" concept is a hack.** It was an attempt to retrofit subtyping into HM unification without changing the fundamental algorithm. It works by making concrete types behave like type variables that can "grow" to include more possibilities when constrained.

**The problem**: This is the wrong mental model. The correct model is **constraint solving**:
- Start with a range of possibilities (any type)
- Each constraint narrows the range
- The final type is the intersection of all constraints

The "growable type" mechanism suggests a value that *changes* over time (choose `Integer`, then change to `Integer|String`). This is misleading. The correct model is a range of possibilities that *shrinks*. The hack was wrong from the start — equality unification is itself a constraint solver (it handles `x == y` constraints), but it's the wrong kind of solver for subtyping. The hack was trying to make an equality solver do the job of a subtype solver.

**The problem**: It only works for **atomic types**, not composite types (lists, tuples).
- `Integer` can "grow" to `Integer|String` ✅
- `[Integer<>>` cannot "grow" to `[Integer|String<>>` ❌

**The result**: Inconsistent behavior. The type checker accepts `append([1,2,3], ["hello"], ?Result)` as a query (because type inference works differently for queries) but rejects it in other contexts.

## 4. The Fix: Replace HM Unification with Constraint Solving

The "growable type" mechanism is the wrong mental model. The fix isn't to make more things "grow" — it's to replace the HM unification engine with a proper constraint solver that works with **ranges of possibilities** from the start.

### Current Architecture
```
Type
├── TVar (type variable)
├── TupleType (container)
├── GrowableType ← "subatomic" type (only wraps atomic types)
│   ├── lowerBound: BoundaryType
│   └── upperBound: BoundaryType
└── BoundaryType (abstract)
    └── CompositeType ← the actual type like Integer, String, [*]<T>
```

### The Problem
HM unification handles `x == y` constraints by immediate binding. It's the wrong kind of solver for subtyping. The "growable type" hack was an attempt to retrofit subtyping into HM unification, but it's the wrong mental model (values that "grow" instead of ranges that "shrink").

### The Fix
Replace HM unification with a constraint solver that:
1. **Starts with ranges of possibilities** (any type for each variable)
2. **Narrows ranges** with each constraint (`x <: y`)
3. **Returns the intersection** of all constraints

This is the correct mental model: constraint solving as narrowing, not as changing values.

### Example
```
Call: append([1,2,3], ["hello"], ?Result)

Step 1: Start with ranges
   ?t = {any type}
   ?Result = {any type}

Step 2: Narrow with constraints
   ?t = {any type} ∩ {lists of Integer}
   ?t = {lists of Integer}
   ?t = {lists of Integer} ∩ {lists of String}
   ?t = {lists of Integer|String}
   ?Result = {any type} ∩ {lists of Integer|String}
   ?Result = {lists of Integer|String}

Step 3: Check consistency
   All ranges non-empty? → Yes → Type checker passes ✅
```

## 5. Modes and Order-Independence

### The Key Constraint
The type system must guarantee correctness **regardless of how the mode system rearranges the query**.

From the tutorial:
> "TyRuBa's type system wants to guarantee that no matter how the subexpressions of a composite expression are rearranged (remember the mode system may choose to change the order!), there will not be any unexpected result in the evaluation."

### Example: `Integer(?x), sum(?x,?x,?2x)`
This fails because the mode system might reorder it to `sum(?x,?x,?2x), Integer(?x)`, and at that point `?x` could be `Object`.

> **Note**: The old tutorial used `sum` with strict types (`=Integer`) as an example, but strict types were removed from TyRuBa. They were a bad idea — "I won't crash if you pass me something else" is procedural thinking, not logical. The current type system has no concept of strict types.

### The Mode System's Role
- Modes define valid execution patterns: `(B,B,F)`, `(F,B,B)`, etc.
- The mode system rearranges subexpressions to satisfy declared execution modes.

### Why This Matters
The type checker can't rely on a specific evaluation order. It must ensure that no matter which order the mode system chooses, the types are consistent. This is why `Integer(?x), sum(?x,?x,?2x)` fails — because there exists an ordering where `?x` is not guaranteed to be `Integer` when `sum` is evaluated.

## 6. Why This Works

The fix doesn't change the type language or the declarations. It changes **how composite types participate in the growable type mechanism**:

- **Current**: Only atomic types can grow → the type checker rejects `[Integer<>>` + `[String<>>`
- **New**: All types can grow → the type checker accepts it

The type system remains descriptive. The type checker still only rejects what's provably inconsistent. But it does so more accurately, allowing valid programs to pass while still catching real errors.

### Example: `append`
```tyruba
append :: [?t], [?t], [?t]
```

Call: `append([1,2,3], ["hello"], ?Result)`

**Current system** (only atomic types grow):
- `[Integer<>> <: [?t]` → `?t = [Integer<>>`
- `[String<>> <: [?t]` → `Integer <: Integer`? No → **fail** ❌

**Fixed system** (all types grow):
- `[Integer<>> <: [?t]` → `?t = [Integer<>>`
- `[String<>> <: [?t]` → `?t` grows to `[Integer|String<>>` → **pass** ✅

## 7. What This Doesn't Fix

- **Runtime unsatisfiability**: `:- odd(4)` is type-consistent but has no solution. The type system can't catch this.
- **Termination**: Infinite loops are not caught by types.
- **Deep logical properties**: The type system is a conservative approximation, not a complete verifier.

This is by design. TyRuBa's types are about **logical consistency**, not complete verification. The type checker catches the obvious mistakes (inconsistent types, impossible relations) and leaves the rest to the logic engine.

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

The contract stays the same:
- Same inputs: AST, predicate info, starting TypeEnv
- Same output: TypeEnv (or throws `TypeModeError`)
- Same side effects: none

**What changes inside:**
- Instead of `argType.intersect(predType[i])` for each arg, collect constraints
- At the end, solve constraints and return narrowed `TypeEnv`

**What doesn't change:**
- `RBExpression.typecheck()` signature
- `prepareForRunning()` doesn't need to change
- The rest of the code doesn't need to know

## API Changes Needed

Only two methods need to change:

1. **`Type.intersect(Type other)`**
   - Currently: Equality unification, throws on failure
   - New: Constraint solving, narrows the range of possibilities

2. **`Type.checkEqualTypes(Type other, boolean grow)`**
   - Currently: HM-style equality unification with "growable" hack
   - New: Subtype constraint collection

Everything else stays the same. The rest of the code calls `typecheck()` and gets back a `TypeEnv`. It doesn't care how that `TypeEnv` was computed.

# ConstraintSolver Unit Tests

## Test Cases

### 1. Basic Type Constraints
- `?a <: Integer` → solve → `?a = Integer`
- `?a <: Number` → solve → `?a = Number`

### 2. Multiple Constraints on Same Variable
- `?a <: Integer`, `?a <: Number` → solve → `?a = Integer` (since Integer <: Number, intersection narrows to Integer)
- `?a :> Integer`, `?a :> Number` → solve → `?a = Number` (since Number :> Integer, intersection narrows to Number)

### 3. Contradiction
- `?a <: Integer`, `?a <: String` → unsatisfiable

### 4. Equality Constraints
- `?a = ?b`, `?a <: Integer` → solve → `?a = Integer`, `?b = Integer`
- `?a = ?b`, `?b = ?c` → solve → `?a = ?b = ?c` (all same type)

### 5. Mixed Constraints
- `?a = ?b`, `?a <: Integer`, `?b <: String` → unsatisfiable
- `?a = ?b`, `?a <: Integer`, `?b <: Number` → solve → `?a = Integer`, `?b = Integer`

### 6. Disjunction (Union)
- Solver A: `?a <: Integer` (solved: `?a = Integer`)
- Solver B: `?a <: String` (solved: `?a = String`)
- Union: `?a = Integer|String`

### 7. Conjunction (Intersect)
- Solver A: `?a <: Integer`
- Solver B: `?a <: Number`
- Intersect: `?a = Integer` (since Integer <: Number)

### 8. Unconstrained Variable
- No constraints on `?a` → solve → `?a = Object` (top of lattice)

### 9. Equality + Type Constraint
- `?a = ?b`, `?a = Integer` → solve → `?a = Integer`, `?b = Integer`

### 10. Complex Equality Chain
- `?a = ?b`, `?b = ?c`, `?c <: Integer` → solve → `?a = Integer`, `?b = Integer`, `?c = Integer`

### 11. Empty Solver
- No constraints → solve → empty TypeEnv

### 12. Multiple Variables
- `?a <: Integer`, `?b <: String` → solve → `?a = Integer`, `?b = String`

### 13. Lists with Unconstrained Type Variables
- `?l <: [*]<T>` → solve → `?l = [*]<T>` (T remains unconstrained, represented as a type variable)
- `?l <: [*]<Integer>` → solve → `?l = [*]<Integer>`
- `?l <: [*]<T>`, `?l <: [*]<Integer>` → solve → `?l = [*]<Integer>` (element type constrained)

### 14. List Element Type Connection (the "aha" moment)
- `?l <: [*]<T>`, `?e <: T`, `?e = Integer` → solve → `?l = [*]<Integer>`, `?e = Integer`, `T = Integer`
- `?l <: [*]<T>`, `?e <: T`, `?e = String` → solve → `?l = [*]<String>`, `?e = String`, `T = String`
- `?l <: [*]<T>`, `?e <: T`, `?e = Integer`, `?e = String` → unsatisfiable (Integer ∩ String = Nothing)

### 15. Chain of List Constraints (append-like)
- `?l1 <: [*]<T>`, `?l2 <: [*]<T>`, `?l1 <: [*]<Integer>`, `?l2 <: [*]<Number>` → solve → `?l1 = [*]<Integer>`, `?l2 = [*]<Integer>`, `T = Integer`
- `?l1 <: [*]<T1>`, `?l2 <: [*]<T2>`, `?l1 = [*]<Integer>`, `?l2 = [*]<String>` → solve → `?l1 = [*]<Integer>`, `?l2 = [*]<String>`, `T1 = Integer`, `T2 = String`

### 16. Complex Multi-Constraint Chain (append pattern)
This models: `append(?l, ?i, ?li), append(?s, ?t, ?st), append(?li, ?st, [1,2,3,4])`

With `append :: [?t], [?t], [?t]` — all three args of each call share the same list type:
- Call 1: `?l, ?i, ?li` all `[*]<T1>`
- Call 2: `?s, ?t, ?st` all `[*]<T2>`
- Call 3: `?li, ?st, [1,2,3,4]` all `[*]<T3>`

Constraints propagate through shared variables:
- `?li` appears in calls 1 and 3 → `T1 = T3`
- `?st` appears in calls 2 and 3 → `T2 = T3`
- `[1,2,3,4]` is `[*]<Integer>` → `T3 = Integer`
- Therefore `T1 = T2 = T3 = Integer`

Solve → `?l = [*]<Integer>`, `?i = [*]<Integer>`, `?li = [*]<Integer>`, `?s = [*]<Integer>`, `?t = [*]<Integer>`, `?st = [*]<Integer>`

If we add `?l = ["a","b"]` → unsatisfiable (String ∩ Integer = Nothing)

### 17. Nested List Types
- `?m <: [[*]<T>]` → solve → `?m = [[*]<T>]`
- `?m <: [[*]<Integer>]`, `?m <: [[*]<Number>]` → solve → `?m = [[*]<Integer>]`

### 18. Proper List vs Cons-Pair
- `?l <: [*]<T>` (proper list) vs `?l <: [*|]<T>` (cons-pair / non-empty list)
- `?l <: [*]<T>`, `?l <: [*|]<T>` → solve → `?l = [*|]<T>` (cons-pair is subtype of proper list, so intersection narrows to cons-pair)
- `?l <: [*|]<T>`, `?l = []` → unsatisfiable (empty list is not a cons-pair)

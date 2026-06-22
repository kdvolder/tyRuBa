package tyRuBa.modes;

import java.util.HashMap;
import java.util.Map;
import tyRuBa.engine.RBVariable;

/**
 * A constraint solver for the TyRuBa type system.
 *
 * Tracks constraints on type variables (TVar). The solver narrows
 * ranges of possibilities by accumulating upper and lower bounds.
 *
 * Key invariants:
 * - Never call solve() during traversal - it's a terminal operation
 * - Constraints are accumulated, never dropped
 * - Unsatisfiable constraints throw TypeModeError
 */
public class ConstraintSolver {

	/**
	 * Core: constraints on type variables.
	 * Maps TVar -> upper bounds (subtype constraints)
	 */
	private final Map<TVar, Type> upperBounds = new HashMap<>();

	/**
	 * Maps TVar -> lower bounds (supertype constraints)
	 */
	private final Map<TVar, Type> lowerBounds = new HashMap<>();

	/**
	 * Convenience: maps RBVariable -> TVar for rule variables.
	 * When we add a constraint on an RBVariable, it creates/uses a TVar.
	 */
	private final Map<RBVariable, TVar> rbVarToTVar = new HashMap<>();

	/**
	 * Create a new empty constraint solver.
	 */
	public static ConstraintSolver make() {
		return new ConstraintSolver();
	}

	private ConstraintSolver() {}

	/**
	 * Assert constraint: sub is a subtype of supr.
	 * Walks both types in parallel, recording constraints on any TVars found.
	 */
	public ConstraintSolver assertSubtypeOf(Type sub, Type supr) throws TypeModeError {
		return _assertSubtypeOf(sub, supr);
	}
	
	private ConstraintSolver _assertSubtypeOf(Type sub, Type supr) throws TypeModeError {
		if (sub instanceof TVar) {
			// In the new world TVars are never mutated/bound via content.
			// They are tracked solely by the solver's constraint maps.
			return _recordUpperBound((TVar)sub, supr);
		}
		if (supr instanceof TVar) {
			return _recordLowerBound((TVar)supr, sub);
		}
		
		if (sub instanceof CompositeType && supr instanceof CompositeType) {
			CompositeType compSub = (CompositeType) sub;
			CompositeType compSupr = (CompositeType) supr;
			if (compSub.getTypeConstructor().equals(compSupr.getTypeConstructor())) {
				TupleType subArgs = compSub.getArgs();
				TupleType suprArgs = compSupr.getArgs();
				for (int i = 0; i < subArgs.size(); i++) {
					_assertSubtypeOf(subArgs.get(i), suprArgs.get(i));
				}
				return this;
			} else {
				if (compSupr.getTypeConstructor().isSuperTypeOf(compSub.getTypeConstructor())) {
					return this;
				}
				throw new TypeModeError("Incompatible types: " + sub + ", " + supr);
			}
		} else if (sub instanceof TupleType && supr instanceof TupleType) {
			TupleType subArgs = (TupleType) sub;
			TupleType suprArgs = (TupleType) supr;
			for (int i = 0; i < subArgs.size(); i++) {
				_assertSubtypeOf(subArgs.get(i), suprArgs.get(i));
			}
			return this;
		}
		
		throw new UnsupportedOperationException(
			"_assertSubtypeOf unhandled: " + sub.getClass().getSimpleName()
			+ " <: " + supr.getClass().getSimpleName());
	}
	
	private ConstraintSolver _recordUpperBound(TVar tvar, Type bound) throws TypeModeError {
		Type existing = upperBounds.get(tvar);
		if (existing != null) {
			upperBounds.put(tvar, existing.intersect(bound, this));
		} else {
			upperBounds.put(tvar, bound);
		}
		return this;
	}
	
	private ConstraintSolver _recordLowerBound(TVar tvar, Type bound) throws TypeModeError {
		Type existing = lowerBounds.get(tvar);
		if (existing != null) {
			lowerBounds.put(tvar, existing.union(bound, this));
		} else {
			lowerBounds.put(tvar, bound);
		}
		return this;
	}
	
	/**
	 * Assert constraint on a type variable: T is a supertype of U
	 */
	public ConstraintSolver assertSuperTypeOf(Type supr, Type sub) throws TypeModeError {
		return assertSubtypeOf(sub, supr);
	}

	/**
	 * Convenience: assert constraint on a rule variable.
	 * Creates a fresh TVar for the rule variable if needed.
	 */
	public ConstraintSolver assertSubtypeOf(RBVariable x, Type t) throws TypeModeError {
		TVar tvar = getOrCreateTVar(x);
		return assertSubtypeOf(tvar, t);
	}

	/**
	 * Convenience: assert constraint on a rule variable.
	 * Creates a fresh TVar for the rule variable if needed.
	 */
	public ConstraintSolver assertSuperTypeOf(RBVariable x, Type t) throws TypeModeError {
		TVar tvar = getOrCreateTVar(x);
		return assertSuperTypeOf(tvar, t);
	}

	private TVar getOrCreateTVar(RBVariable x) {
		TVar tvar = rbVarToTVar.get(x);
		if (tvar == null) {
			tvar = Factory.makeTVar(x.name());
			rbVarToTVar.put(x, tvar);
		}
		return tvar;
	}

	/**
	 * Solve the constraints and return a TypeEnv with the narrowed types.
	 * This is a terminal operation - call it exactly once, after all
	 * constraints have been added.
	 */
	public TypeEnv solve() throws TypeModeError {
		TypeEnv env = Factory.makeTypeEnv();

		// Map RBVariables to their solved types
		for (Map.Entry<RBVariable, TVar> entry : rbVarToTVar.entrySet()) {
			RBVariable x = entry.getKey();
			TVar tvar = entry.getValue();

			Type upper = upperBounds.get(tvar);
			Type lower = lowerBounds.get(tvar);

			Type solvedType;
			if (upper != null && lower != null) {
				// Both bounds: must be compatible, intersect them
				if (!lower.isSubTypeOf(upper, null)) {
					throw new TypeModeError("Incompatible constraints: " + lower + " <: " + upper);
				}
				solvedType = upper.intersect(lower, this);
			} else if (upper != null) {
				// Only upper bound
				solvedType = upper;
			} else if (lower != null) {
				// Only lower bound
				solvedType = lower;
			} else {
				// No constraints: most general type
				solvedType = Type.object;
			}

			env.put(x, solvedType);
		}

		return env;
	}

	/**
	 * Check if a TVar has any constraints recorded in this solver.
	 */
	public boolean isConstrained(TVar tvar) {
		return upperBounds.containsKey(tvar) || lowerBounds.containsKey(tvar);
	}

	/**
	 * Dump the internal state of the solver for debugging/testing.
	 */
	public String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("Solver{");
		boolean first = true;

		// Show TVar constraints
		for (TVar tvar : upperBounds.keySet()) {
			if (!first) sb.append(", ");
			first = false;
			Type upper = upperBounds.get(tvar);
			Type lower = lowerBounds.get(tvar);
			sb.append(tvar).append(": ");
			if (upper != null && lower != null) {
				sb.append("[");
				sb.append(lower);
				sb.append(", ");
				sb.append(upper);
				sb.append("]");
			} else if (upper != null) {
				sb.append("[: ");
				sb.append(upper);
				sb.append("]");
			} else if (lower != null) {
				sb.append("[ ");
				sb.append(lower);
				sb.append(": ]");
			}
		}

		sb.append("}");
		return sb.toString();
	}
}

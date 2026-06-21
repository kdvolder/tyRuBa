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
	 * Assert constraint on a type variable: T is a subtype of U
	 */
	public ConstraintSolver assertSubtypeOf(TVar t, Type u) throws TypeModeError {
		Type existing = upperBounds.get(t);
		if (existing != null) {
			upperBounds.put(t, existing.intersect(u, this));
		} else {
			upperBounds.put(t, u);
		}
		return this;
	}
	
	/**
	 * Assert constraint on a type variable: T is a supertype of U
	 */
	public ConstraintSolver assertSuperTypeOf(TVar t, Type u) throws TypeModeError {
		Type existing = lowerBounds.get(t);
		if (existing != null) {
			lowerBounds.put(t, existing.union(u));
		} else {
			lowerBounds.put(t, u);
		}
		return this;
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
		
		// Solve each TVar constraint
		for (Map.Entry<TVar, Type> entry : upperBounds.entrySet()) {
			TVar tvar = entry.getKey();
			Type upper = entry.getValue();
			Type lower = lowerBounds.get(tvar);
			
			Type solvedType;
			if (lower != null) {
				// Both bounds
				if (!lower.isSubTypeOf(upper, null)) {
					throw new TypeModeError("Incompatible constraints: " + lower + " <: " + upper);
				}
				solvedType = upper.intersect(lower);
			} else {
				solvedType = upper;
			}
			
			// Bind the TVar to its solved type
			if (tvar.isFree()) {
				tvar.setContents(solvedType);
			}
		}
		
		// Map RBVariables to their solved types
		for (Map.Entry<RBVariable, TVar> entry : rbVarToTVar.entrySet()) {
			RBVariable x = entry.getKey();
			TVar tvar = entry.getValue();
			env.put(x, tvar.isFree() ? tvar : tvar.getContents());
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

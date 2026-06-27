package tyRuBa.modes;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

abstract public class Type implements Cloneable, Serializable {

	public static final Type integer = Factory.makeJavaType(Integer.class);
	public static final Type longint = Factory.makeJavaType(Long.class);
	public static final Type string = Factory.makeJavaType(String.class);
	public static final Type number = Factory.makeJavaType(Number.class);
	public static final Type object = Factory.makeJavaType(Object.class);	

	/**
	 * @see #checkEqualTypes(Type, boolean)
	 */
	@Deprecated
	public void checkEqualTypes(Type other) throws TypeModeError {
		checkEqualTypes(other, true);
	}

	/**
	 * @deprecated Legacy HM-unification method. In the old world this was the
	 * universal hammer for expressing type constraints — subtype, supertype,
	 * and true equality were all expressed as equality unification, resolved
	 * by letting subatomic types grow toward a common type. The {@code grow}
	 * parameter controlled whether growth was allowed (subtype constraint)
	 * or not (strict equality).
	 * <p>
	 * The replacement depends on the original intent of each call site. See
	 * each call site for guidance on which constraint to use.
	 */
	@Deprecated
	public abstract void checkEqualTypes(Type other, boolean grow) throws TypeModeError;
	
	/** return true if this contains reference to var */
	public abstract boolean isFreeFor(TVar var);
	
	/** return a clone of this, making sure that each TVar is only cloned
	 *  once */
	public abstract Type clone(Map varRenamings);

	/**
	 * @deprecated Use {@link #union(Type, ConstraintSolver)} instead. The old
	 * API is unaware of any type constraints already existing on type variables
	 * tracked by the ConstraintSolver.
	 */
	@Deprecated
	public abstract Type union(Type other) throws TypeModeError;
	
	/**
	 * @deprecated Use {@link #intersect(Type, ConstraintSolver)} instead. The old
	 * API is unaware of any type constraints already existing on type variables
	 * tracked by the ConstraintSolver.
	 */
	@Deprecated
	public abstract Type intersect(Type other) throws TypeModeError;
	
	public Type intersect(Type other, ConstraintSolver solver) throws TypeModeError {
		throw new UnsupportedOperationException("not yet implemented: " + this.getClass().getName());
	}
	
	public Type union(Type other, ConstraintSolver solver) throws TypeModeError {
		throw new UnsupportedOperationException("not yet implemented: " + this.getClass().getName());
	}
	
	/** return the smallest super type of this and other */
//	abstract Type lowerBound(Type other) throws TypeModeError;

	public static void check(boolean b, Type t1, Type t2) throws TypeModeError {
		if (!b) {
			throw new TypeModeError("Incompatible types: " + t1 + ", " + t2);
		}
	}
	
	public abstract boolean isSubTypeOf(Type type, Map renamings);

	public abstract boolean hasOverlapWith(Type other);
	
	public boolean hasOverlapWith(Vector types, boolean hasOverlap) {
		int size = types.size();
		boolean equalTypes = false;
		int counter = 0;
		while (!equalTypes && counter < size) {
			Type currType = (Type)types.elementAt(counter);
			if (currType.equals(this)) {
				hasOverlap = hasOverlap && true;
				equalTypes = true;
			} else if (hasOverlap) {
				if (!currType.hasOverlapWith(this)) {
					hasOverlap = false;
				}
			}
			counter++;
		}
		if (!equalTypes) {
			types.add(this);
		}
		return hasOverlap;
	}

	abstract public Type getParamType(String currName, Type repAs);
	
	public Class javaEquivalent() throws TypeModeError {
	    throw new TypeModeError("This type "+this+" has no defined mapping to a Java equivalent");
	}

    public boolean isJavaType() {
        return false;
    }

	public abstract Type makeStrict();

	/**
	 * Check if a TVar occurs in this type structure, resolving
	 * constrained TVars through the solver's constraint maps.
	 * This is a proper occurs check that follows the constraint
	 * graph rather than just the type structure.
	 */
	public boolean contains(TVar tvar, ConstraintSolver solver) {
		throw new UnsupportedOperationException("not yet implemented: " + this.getClass().getName());
	}

}
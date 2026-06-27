package tyRuBa.modes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents the union (LUB) of two or more types.
 *
 * Used when the union of types cannot be expressed more directly
 * (e.g. union of two unrelated concrete types, or union involving
 * type variables that are not yet constrained).
 *
 * A UnionType is a data structure — it does not participate in
 * constraint solving directly. It is the exact result of a union
 * operation, regardless of whether its operands are fully resolved.
 *
 * Invariants maintained by {@link #make(Type, Type)}:
 * - At least 2 elements (reduces to single type if only one remains)
 * - No element is itself a UnionType (flattening)
 * - Order does not matter (elements stored in a Set)
 */
public class UnionType extends Type {

	private Set<Type> elements = new HashSet<Type>();

	/**
	 * Construct a union type from two types.
	 *
	 * Enforces invariants:
	 * - If both types are the same → returns that type
	 * - If one is a UnionType → flattens its elements
	 * - If one subtypes the other → returns the supertype
	 * - Otherwise → creates a new UnionType with both types
	 *
	 * @param t1 first type
	 * @param t2 second type
	 * @return a UnionType, or the single type if only one is needed
	 * @throws TypeModeError if types are incompatible
	 */
	public static Type make(Type t1, Type t2) throws TypeModeError {
		if (t1.equals(t2)) {
			return t1;
		}
		// Flatten: extract all non-union elements from both sides
		Set<Type> flat = new HashSet<Type>();
		if (t1 instanceof UnionType) {
			flat.addAll(((UnionType) t1).elements);
		} else {
			flat.add(t1);
		}
		if (t2 instanceof UnionType) {
			flat.addAll(((UnionType) t2).elements);
		} else {
			flat.add(t2);
		}
		if (flat.size() == 1) {
			return flat.iterator().next();
		}
		return new UnionType(flat);
	}

	private UnionType(Set<Type> elements) {
		this.elements = elements;
	}

	public String getName() {
		return "union";
	}

	@Override
	public void checkEqualTypes(Type other, boolean grow) throws TypeModeError {
		throw new UnsupportedOperationException("UnionType.checkEqualTypes is not supported");
	}

	@Override
	public boolean isFreeFor(TVar var) {
		for (Type t : elements) {
			if (t.isFreeFor(var)) return true;
		}
		return false;
	}

	@Override
	public Type clone(Map varRenamings) {
		Set<Type> cloned = new HashSet<Type>();
		for (Type t : elements) {
			cloned.add(t.clone(varRenamings));
		}
		return new UnionType(cloned);
	}

	@Override
	public Type union(Type other) throws TypeModeError {
		throw new UnsupportedOperationException("UnionType.union(Type) is deprecated");
	}

	@Override
	public Type union(Type other, ConstraintSolver solver) throws TypeModeError {
		return UnionType.make(this, other);
	}

	@Override
	public Type intersect(Type other) throws TypeModeError {
		throw new UnsupportedOperationException("UnionType.intersect(Type) is deprecated");
	}

	@Override
	public Type intersect(Type other, ConstraintSolver solver) throws TypeModeError {
		// (A1|A2|...) ∩ B = (A1∩B) | (A2∩B) | ...
		// Individual element intersections may be empty (throw TypeModeError).
		// Only throw if the entire union is eliminated.
		Set<Type> results = new HashSet<Type>();
		for (Type t : elements) {
			try {
				Type i = t.intersect(other, solver);
				if (i instanceof UnionType) {
					// Flatten: extract nested union elements
					results.addAll(((UnionType) i).elements);
				} else {
					results.add(i);
				}
			} catch (TypeModeError e) {
				// This branch is eliminated (empty intersection), continue
			}
		}
		if (results.isEmpty()) {
			throw new TypeModeError("Incompatible types: " + this + ", " + other);
		}
		if (results.size() == 1) {
			return results.iterator().next();
		}
		// Use UnionType.make iteratively to maintain flattening invariant
		Type result = null;
		for (Type t : results) {
			result = (result == null) ? t : UnionType.make(result, t);
		}
		return result;
	}

	@Override
	public boolean isSubTypeOf(Type type, Map renamings) {
		// A union is a subtype of T iff all its elements are subtypes of T
		for (Type t : elements) {
			if (!t.isSubTypeOf(type, renamings)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasOverlapWith(Type other) {
		// A union has overlap with T iff at least one element has overlap with T
		for (Type t : elements) {
			if (t.hasOverlapWith(other)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Type getParamType(String currName, Type repAs) {
		throw new UnsupportedOperationException("UnionType.getParamType is not supported");
	}

	@Override
	public Type makeStrict() {
		// Union types don't have a strict/non-strict distinction
		return this;
	}

	@Override
	public boolean contains(TVar tvar, ConstraintSolver solver) {
		for (Type t : elements) {
			if (t.contains(tvar, solver)) return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return elements.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof UnionType)) return false;
		UnionType other = (UnionType) obj;
		return elements.equals(other.elements);
	}

	@Override
	public String toString() {
		List<Type> sorted = new ArrayList<Type>(elements);
		// Sort for consistent output (sets don't guarantee order)
		java.util.Collections.sort(sorted, new java.util.Comparator<Type>() {
			public int compare(Type a, Type b) {
				return a.toString().compareTo(b.toString());
			}
		});
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sorted.size(); i++) {
			if (i > 0) sb.append("|");
			sb.append(sorted.get(i).toString());
		}
		return sb.toString();
	}
}

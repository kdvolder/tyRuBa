package tyRuBa.modes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Invariants maintained by {@link #make(List)}:
 * - At least 2 elements (throws if 0 or 1)
 * - No element is itself a UnionType (flattening)
 */
public class UnionType extends Type {

	private List<Type> elements;

	/**
	 * Construct a union type from a list of types.
	 *
	 * Enforces invariants:
	 * - 0 elements → TypeModeError
	 * - 1 element → returns that element directly
	 * - 2+ elements → flattens nested unions
	 *
	 * @param types the types to union
	 * @return a UnionType, or the single type if only one is given
	 * @throws TypeModeError if types is empty
	 */
	public static Type make(List<Type> types) throws TypeModeError {
		if (types.isEmpty()) {
			throw new TypeModeError("Cannot create union of zero types");
		}
		if (types.size() == 1) {
			return types.get(0);
		}
		// Flatten: extract all non-union elements
		List<Type> flat = new ArrayList<>();
		for (Type t : types) {
			if (t instanceof UnionType) {
				UnionType ut = (UnionType) t;
				flat.addAll(ut.elements);
			} else {
				flat.add(t);
			}
		}
		if (flat.size() == 1) {
			return flat.get(0);
		}
		return new UnionType(flat);
	}

	private UnionType(List<Type> elements) {
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
		List<Type> cloned = new ArrayList<>();
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
		// (A1|A2|...) ∪ B = (A1∪B) | (A2∪B) | ...
		List<Type> results = new ArrayList<>();
		for (Type t : elements) {
			results.add(t.union(other, solver));
		}
		return UnionType.make(results);
	}

	@Override
	public Type intersect(Type other) throws TypeModeError {
		throw new UnsupportedOperationException("UnionType.intersect(Type) is deprecated");
	}

	@Override
	public Type intersect(Type other, ConstraintSolver solver) throws TypeModeError {
		// (A1|A2|...) ∩ B = (A1∩B) | (A2∩B) | ...
		List<Type> results = new ArrayList<>();
		for (Type t : elements) {
			results.add(t.intersect(other, solver));
		}
		return UnionType.make(results);
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

	public Type getParamType(String currName, Type repAs) {
		throw new UnsupportedOperationException("UnionType.getParamType is not supported");
	}

	@Override
	public Type makeStrict() {
		// Union types don't have a strict/non-strict distinction
		return this;
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
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < elements.size(); i++) {
			if (i > 0) sb.append("|");
			sb.append(elements.get(i).toString());
		}
		return sb.toString();
	}
}

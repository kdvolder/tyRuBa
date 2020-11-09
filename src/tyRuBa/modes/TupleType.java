/*****************************************************************\
 * File:        TypeList.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import java.util.ArrayList;
import java.util.Map;

public final class TupleType extends Type {

    private static final long serialVersionUID = 1L;
    
    private ArrayList<Type> parts;

	/** default Constructor */
	public TupleType() {
		parts = new ArrayList<Type>();
	}
	
	/** Constructor */
	public TupleType(Type[] types) {
		parts = new ArrayList<Type>(types.length);
		for (int i = 0; i < types.length; i++) {
			parts.add(types[i]);
		}
	}

	public Type[] getTypes() {
		return (Type[]) parts.toArray(new Type[parts.size()]);
	}

	public int hashCode() {
		final int size = size();
		int hash = getClass().hashCode();
		for (int i = 0; i < size; i++)
			hash = hash * 19 + get(i).hashCode();
		return hash;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		} else if (! (other instanceof TupleType)) {
			return false;
		} else {
			tyRuBa.modes.TupleType cother = (TupleType) other;
			int size = size();
			if (size != cother.size()) {
				return false;
			} else {
				for (int i = 0; i < size(); i++) {
					Type t1 = get(i);
					Type t2 = cother.get(i);
					if (t1 == null && t2 != null) {
						return false;
					} else if (t1 != null && t2 == null) {
						return false;
					} else if (t1 != null && !(get(i).equals(cother.get(i)))) {
						return false;
					}
				}
				return true;
			}
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer("<");
		for (int i = 0; i < size(); i++) {
			if (i != 0) {
				result.append(", ");
			}
			result.append(get(i));
		}
		result.append(">");
		return result.toString();
	}

	public void add(Type newPart) {
		parts.add(newPart);
	}

	public Type get(int i) {
		return (Type) parts.get(i);
	}

	public int size() {
		return parts.size();
	}

	public void checkEqualTypes(Type tother, boolean grow) throws TypeModeError {
		if (tother instanceof TVar) {
			tother.checkEqualTypes(this, grow);
		} else {
			check(tother instanceof TupleType, this, tother);
			TupleType other = (TupleType) tother;
			check(other.size() == size(), this, other);
			for (int i = 0; i < this.size(); i++) {
				Type t1 = this.get(i);
				Type t2 = other.get(i);
				t1.checkEqualTypes(t2, grow);
			}
		}
	}
	
	public boolean isSubTypeOf(Type tother, Map renamings) {
		if (!(tother instanceof TupleType)) {
			return false;
		} else {
			TupleType other = (TupleType) tother;
			if (size() != other.size()) {
				return false;
			} else {
				for (int i = 0; i < size(); i++) {
					if (!get(i).isSubTypeOf(other.get(i), renamings)) {
						return false;
					}
				}
				return true;
			}
		}
	}

	public Type clone(Map tfact) {
		TupleType result = Factory.makeTupleType();
		for (int i = 0; i < size(); i++) {
			if (get(i) == null) {
				result.add(null);
			} else {
				result.add(get(i).clone(tfact));
			}
		}
		return result;
	}
	
	public Type union(Type tother) throws TypeModeError {
		if (tother instanceof TVar) {
			return tother.union(this); 
		} else {
			check(tother instanceof TupleType, this, tother);
			TupleType other = (TupleType) tother;
			check(other.size() == size(), this, other);
			TupleType result = Factory.makeTupleType();
			for (int i = 0; i < size(); i++) {
				if (get(i) == null) {
					result.add(other.get(i));
				} else {
					result.add(get(i).union(other.get(i)));
				}
			}
			return result;
		}
	}

	public Type intersect(Type tother) throws TypeModeError {
		if (tother instanceof TVar) {
			return tother.intersect(this);
		}
		check(tother instanceof TupleType, this, tother);
		TupleType other = (TupleType) tother;
		check(other.size() == size(), this, other);
		TupleType result = Factory.makeTupleType();
		for (int i = 0; i < size(); i++) {
			if (get(i) == null) {
				result.add(other.get(i));
			} else {
				result.add(get(i).intersect(other.get(i)));
			}
		}
		return result;
	}

	public boolean hasOverlapWith(Type tother) {
		if (tother == null) {
			return false;
		} else if (tother instanceof TVar) {
			return tother.hasOverlapWith(tother);
		} else if (! (tother instanceof TupleType)) {
			return false;
		} else {
			TupleType other = (TupleType) tother;
			int size = size();
			if (size != other.size()) {
				return false;
			} else {
				for (int i = 0; i < size(); i++) {
					if (get(i) == null && other.get(i) != null) {
						return false;
					} else if (get(i) != null
							&& ! get(i).hasOverlapWith(other.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
	}

	public boolean isFreeFor(TVar var) {
		for (int i = 0; i < size(); i++) {
			if (get(i) != null && ! get(i).isFreeFor(var)) {
				return false;
			}
		}
		return true;
	}

	public Type getParamType(int pos, TypeConstructor typeConst) {
//		if (! typeConst.hasRepresentation()) {
			return get(pos);
//		} else {
//			return getParamType(typeConst.getParameterName(pos),
//					typeConst.getRepresentation());
//		}
	}

	public Type getParamType(String currName, Type repAs) {
		if (repAs instanceof TVar) {
			if (currName.equals(((TVar) repAs).getName())) {
				return this; 
			} else {
				return null;
			}
		} else if (! (repAs instanceof TupleType)) {
			return null; 
		} else {
			Type result = null;
			TupleType repAsTuple = (TupleType) repAs;
			if (size() != repAsTuple.size()) {
				return null;
			} else {
				for (int i = 0; i < size(); i++) {
					Type currParamType = get(i).getParamType(currName, repAsTuple.get(i));
					if (result != null && currParamType != null) {
						try {
							result.checkEqualTypes(currParamType);
						} catch (TypeModeError e) {
							return null;
						}
					} else if (result == null) {
						result = currParamType;
					}
			
				}
				return result;
			}
		}
	}

	/**
	 * Project a tuple type by only retaining the elements
	 * that occur in "Bound" places. Used to obtain the type
	 * of an index's key.
	 * 
	 * @param list
	 * @return TupleType projected type
	 */
	public TupleType project(BindingList list) {
		ArrayList<Type> keepTypes = new ArrayList<Type>();
		for (int i = 0; i < list.size(); i++) {
			BindingMode b = list.get(i);
			if (b.isBound())
				keepTypes.add(get(i));
			else if (b.usePartialKeyExtraction()) {
				keepTypes.add(b.partialKeyType(get(i)));
			}
		}
		return new TupleType(keepTypes.toArray(new Type[keepTypes.size()]));
	}

	@Override
	public Type makeStrict() {
		Type[] newParts = new Type[parts.size()];
		for (int i = 0; i < newParts.length; i++) {
			newParts[i] = parts.get(i).makeStrict();
		}
		return new TupleType(newParts);
	}
    
}

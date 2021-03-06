//package tyRuBa.modes;
//
//import java.util.Map;
//
//public final class OldListType extends Type {
//
//	private Type element;
//
//	public OldListType() {
//		element = null; // indicates empty list, doesn't have elements!
//	}
//
//	public OldListType(Type element) {
//		this.element = element;
//	}
//
//	public int hashCode() {
//		return getElementType() == null ? 23423 : getElementType().hashCode() + 5774;
//	}
//	
//	public boolean equals(Object other) {
//		if (other instanceof OldListType) {
//			OldListType cother = (OldListType) other;
//			if (this.getElementType() == null) {
//				return cother.getElementType() == null;
//			} else { 
//				return cother.getElementType() != null 
//					&& (getElementType().equals(cother.getElementType()));
//			}
//		} else
//			return false;
//	}
//
//	public String toString() {
//		if (getElementType() == null)
//			return "[]";
//		else
//			return "[" + getElementType() + "]";
//	}
//
//  	/** Be warned... this may return null if invoked on EmptyList type! */
//	public Type getElementType() {
//		return element;
//	}
//
//	public boolean isFreeFor(TVar var) {
//		return getElementType() == null || getElementType().isFreeFor(var);
//	}
//
//	public Type clone(Map tfact) {
//		return new OldListType(getElementType() == null ? null : getElementType().clone(tfact));
//	}
//
//	public Type intersect(Type other) throws TypeModeError {
//		if (other instanceof TVar) {
//			return other.intersect(this);
//		} else if (this.equals(other)) {
//			return this;
//		} else {
//			check(other instanceof OldListType, this, other);
//			OldListType lother = (OldListType)other;
//			if (this.getElementType() == null) {
//				return this;
//			} else if (lother.getElementType() == null) {
//				return lother;
//			} else { 
//				return new OldListType(
//					this.getElementType().intersect(lother.getElementType()));
//			}
//		}
//	}
//	
//	public void checkEqualTypes(Type other, boolean grow) throws TypeModeError {
//		if (other instanceof TVar)
//			other.checkEqualTypes(this, grow);
//		else {
//			check(other instanceof OldListType, this, other);
//			OldListType lother = (OldListType)other;
//			if (getElementType() == null) {
////				element = lother.getElementType();
//			} else if (lother.getElementType() == null) {
//				lother.element = getElementType();
//			} else {
//				try {
//					this.getElementType().checkEqualTypes(lother.getElementType(), grow);
//				} catch (TypeModeError e) {
//					throw new TypeModeError(e, this);
//				}
//			}
//		}
//	}
//
//	public boolean isSubTypeOf(Type declared, Map renamings) {
//		if (declared instanceof TVar)
//			declared = ((TVar)declared).getContents();
//		if (declared == null) // Was a free TVar
//			return false;
//		if (declared instanceof OldListType) {
//			OldListType ldeclared = (OldListType) declared;
//			if (getElementType() == null) {
//				return true;
//			} else if (ldeclared.getElementType() == null) {
//				return false;
//			} else {
//				return getElementType().isSubTypeOf(ldeclared.getElementType(), renamings);
//			}
//		} else {
//			return false;
//		}
//	}
//	
//	public boolean hasOverlapWith(Type other) {
//		if (other instanceof TVar) {
//			return other.hasOverlapWith(this);
//		} else if (other instanceof OldListType) {
//			Type otherElement = ((OldListType) other).element;
//			if (element == null && otherElement == null) {
//				return true;
//			} else if (element == null || otherElement == null) {
//				return false;
//			} else {
//				return element.hasOverlapWith(otherElement);
//			}
//		} else {
//			return false;
//		}
//	}
//
//	public Type copyStrictPart() {
//		if (element == null) {
//			return new OldListType();
//		} else {
//			return new OldListType(element.copyStrictPart());
//		}
//	}
//
////	Type lowerBound(Type other) throws TypeModeError {
////		if (other instanceof TVar) {
////			return other.lowerBound(this);
////		}
////		check(other instanceof ListType, this, other);
////		ListType lother = (ListType) other;
////		return new ListType(element.lowerBound(lother.element));
////	}
//
//	public Type union(Type other) throws TypeModeError {
//		if (other instanceof TVar) {
//			return other.union(this);
//		} else {
//			check(other instanceof OldListType, this, other);
//			OldListType lother = (OldListType)other;
//			if (getElementType() == null) {
//				return other;
//			} else if (lother.getElementType() == null) {
//				return this;
//			} else {
//				return new OldListType(
//					this.getElementType().union(lother.getElementType()));
//			}
//		}
//	}
//	
//	public Type getParamType(String currName, Type repAs) {
//		if (repAs instanceof TVar) {
//			if (currName.equals(((TVar)repAs).getName())) {
//				return this;
//			} else {
//				return null;
//			}
//		} else if (! (repAs instanceof OldListType)) {
//			return null;
//		} else if (element == null) {
//			return null;
//		} else {
//			return element.getParamType(currName, ((OldListType)repAs).element);
//		}
//	}
//
//	@Override
//	public Type makeStrict() {
//		return new OldListType(element.makeStrict());
//	}
//
//}

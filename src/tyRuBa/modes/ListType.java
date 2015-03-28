package tyRuBa.modes;


/**
 * ListType is no longer a "real" class it has been refactored
 * to be equivalent to a user defined type. This class only has
 * static methods to create the appropriate TypeConstructors and
 * such.
 * 
 * @author kdvolder
 */
public class ListType {

	private static final TypeConstructor listTC;
	private static final TypeConstructor emptyListTC;
	private static final TypeConstructor pairListTC;
	
	static {
		try {
			
			listTC = Factory.makeTypeConstructor("[*]",1);
			pairListTC = Factory.makeTypeConstructor("[*|]",1);
			emptyListTC = Factory.makeTypeConstructor("[]", 0);

			Type listEl = Factory.makeTVar("ListEl");
			Type list   = listTC.apply(Factory.makeTupleType(listEl), true);
			
			listTC.setParameter(Factory.makeTupleType(listEl));
			
			emptyListTC.setParameter(Factory.makeTupleType());
			emptyListTC.setRepresentationType(Factory.makeTupleType());
			emptyListTC.addSuperTypeConst(listTC);
			
			pairListTC.setParameter(Factory.makeTupleType(listEl));
			pairListTC.setRepresentationType(
					Factory.makeTupleType(listEl, list));
			pairListTC.addSuperTypeConst(listTC);
			
		} catch (TypeModeError e) {
			throw new Error(e);
		}
	}

	public static Type make(Type et) {
		return listTC.apply(Factory.makeTupleType(et), false);
	}

	public static Type makeEmpty() {
		return emptyListTC.apply(Factory.makeTupleType(), true);
	}

	public static Type makeNonEmpty(Type et) {
		return pairListTC.apply(Factory.makeTupleType(et), true);
	}

}

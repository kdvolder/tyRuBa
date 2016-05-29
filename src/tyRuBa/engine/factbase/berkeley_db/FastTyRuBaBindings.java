package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.tuple.TupleBinding;

import tyRuBa.modes.CompositeType;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;

public class FastTyRuBaBindings extends TyRuBaBindings {
	
	public FastTyRuBaBindings(BerkeleyDBBasedPersistence env) {
		super(env);
//		Type sourceLocType;
//		try {
//			sourceLocType = frontend.findType("ca.ubc.jquery.ast.SourceLocation");
//		} catch (TypeModeError e) {
//			throw new Error(e);
//		}
//		bindings.put(sourceLocType,new TupleBinding() {
//
//			@Override
//			public Object entryToObject(TupleInput in) {
//				String s = in.readString();
//				int p = in.readInt();
//				int l = in.readInt();
//				int ctr = in.readInt();
//				return RBJavaObjectCompoundTerm.makeJava(
//						new SourceLocation(s,p,l,ctr));
//			}
//
//			@Override
//			public void objectToEntry(Object obj, TupleOutput out) {
//				SourceLocation loc = (SourceLocation) ((RBJavaObjectCompoundTerm) obj).getObject();
//				out.writeString(loc.locationID);
//				out.writeInt(loc.start);
//				out.writeInt(loc.length);
//				out.writeInt(loc.position);
//			}
//			
//		});
	}

	@Override
	protected TupleBinding makeEntryBinding(Type type) {
		if (type instanceof TupleType) {
			TupleType tup = (TupleType)type;
			TupleBinding[] parts = new TupleBinding[tup.size()];
			for (int i = 0; i < parts.length; i++) {
				parts[i]=get(tup.get(i));
			}
			return TyRuBaTupleBinding.make(parts);
		}
		else if (type instanceof CompositeType) {
			CompositeType comp_type = (CompositeType) type;
			TypeConstructor tc = comp_type.getTypeConstructor();
			if (tc.hasRepresentation()) {
				return new TyRuBaRepAsBinding(tc.getConstructorType(),get(tc.getRepresentation()));
			}
			else {
				System.out.println(tc);
			}
		}
		return super.makeEntryBinding(type);
	}

}

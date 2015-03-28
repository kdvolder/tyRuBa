package tyRuBa.modes;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;

/**
 * 
 * @author kdvolder
 *
 */
public class BoundComposite extends PartiallyBound {
	
	private ConstructorType cons; 
	private BindingList args;
	
	public BoundComposite(ConstructorType cons, BindingList args) {
		super();
		assert !args.isAllBound();
		this.cons = cons;
		this.args = args;
	}

	@Override
	public boolean satisfyBinding(BindingMode mode) {
		return mode.isFree() || this.equals(mode);
	}
	
	public String toString() {
		if (cons==null || args==null) return super.toString();
		StringBuffer result = new StringBuffer();
		result.append(cons.getFunctorId().getName());
		result.append("<");
		for (int i = 0; i < args.size(); i++) {
			if (i>0) result.append(",");
			result.append(args.get(i).toString());
		}
		result.append(">");
		return result.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof BoundComposite) {
			BoundComposite o = (BoundComposite) other;
			return cons.equals(o.cons) && args.equals(o.args);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = cons.hashCode();
		for (int i = 0; i < args.size(); i++) {
			hash = hash * 13 + args.hashCode();
		}
		return hash;
	}

	@Override
	public String getBFString() {
		StringBuffer r = new StringBuffer();
		r.append("_");
		r.append(cons.getFunctorId().getName());
		r.append("(");
		for (int i = 0; i < args.size(); i++)
			r.append(args.get(i).getBFString());
		r.append(")");
		return r.toString();
	}

	@Override
	public boolean usePartialKeyExtraction() {
		return args.getNumBound()>0;
	}

	@Override
	public RBTerm extractPartialKey(RBTerm term) {
		if (term instanceof RBCompoundTerm) {
			RBCompoundTerm compound = (RBCompoundTerm) term;
			if (!compound.getConstructorType().equals(cons))
				throw new Error("This case is not supported (yet)!");
			return ((RBTuple)compound.getArg()).project(args);
		}
		else 
			throw new Error("This should not happen");
	}

	@Override
	public Type partialKeyType(Type type) {
		if (type instanceof CompositeType) {
			CompositeType compound = (CompositeType) type;
			TypeConstructor typeCons = compound.getTypeConstructor();
			Type rep = typeCons.getRepresentation();
			ConstructorType repCons = typeCons.getConstructorType();
			if (rep instanceof TupleType && repCons.equals(cons)) {
				TupleType tupleRep = (TupleType) rep;
				return tupleRep.project(args);
			}
			throw new Error("This case is not supported (yet)!");
		}
		else 
			throw new Error("This should not happen");
	}

}

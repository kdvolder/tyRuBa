package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.TypeModeError;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class TyRuBaRepAsBinding extends TupleBinding {

	private ConstructorType constructor;
	private TupleBinding repBinding;

	public TyRuBaRepAsBinding(ConstructorType constructorType, TupleBinding repBinding) {
		this.constructor = constructorType;
		this.repBinding = repBinding;
	}

	@Override
	public Object entryToObject(TupleInput in) {
		try {
            Object rep = repBinding.entryToObject(in);
            return constructor.apply((RBTerm) rep);
        } catch (TypeModeError e) {
            throw new IllegalStateException(e);
        }
	}

	@Override
	public void objectToEntry(Object obj, TupleOutput out) {
		RBCompoundTerm term = (RBCompoundTerm) obj;
		repBinding.objectToEntry(term.getArg(), out);
	}

}

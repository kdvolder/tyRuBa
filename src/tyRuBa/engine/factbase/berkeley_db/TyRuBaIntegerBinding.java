package tyRuBa.engine.factbase.berkeley_db;

import java.io.IOException;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBJavaObjectCompoundTerm;
import annotations.Feature;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

@Feature(names="./BDB")
public class TyRuBaIntegerBinding extends TupleBinding {

	@Override public Object entryToObject(TupleInput in) {
		return FrontEnd.makeInteger(in.readInt());
	}

	@Override public void objectToEntry(Object o, TupleOutput out) {
		RBJavaObjectCompoundTerm t = (RBJavaObjectCompoundTerm) o;
		out.writeInt(((Integer)t.getObject()).intValue());
	}

}

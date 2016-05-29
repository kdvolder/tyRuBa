package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBTerm;

public class TyRuBaStringBinding extends TupleBinding {

	@Override
	public Object entryToObject(TupleInput in) {
		return FrontEnd.makeName(in.readString());
	}

	@Override
	public void objectToEntry(Object o, TupleOutput out) {
		RBTerm t = (RBTerm) o;
		out.writeString(t.quotedToString());
	}

}

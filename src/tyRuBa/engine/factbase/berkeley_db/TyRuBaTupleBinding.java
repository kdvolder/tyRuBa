package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;

import annotations.Feature;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

@Feature(names="./BDB")
public class TyRuBaTupleBinding extends TupleBinding {

	private static TupleBinding theEmpty = new TupleBinding() {

		@Override
		public Object entryToObject(TupleInput in) {
			return RBTuple.theEmpty;
		}

		@Override
		public void objectToEntry(Object o, TupleOutput out) {
		}
		
		public String toString() {
			return "<>";
		}
		
	};
	
	private TupleBinding[] parts;

	private TyRuBaTupleBinding(TupleBinding[] parts) {
		this.parts = parts;
	}

	public static TupleBinding make(TupleBinding[] parts) {
		if (parts.length==0)
			return theEmpty;
		else
			return new TyRuBaTupleBinding(parts);
	}

	@Override
	public Object entryToObject(TupleInput in) {
		RBTerm[] terms = new RBTerm[parts.length]; 
		for (int i = 0; i < terms.length; i++) {
			terms[i]=(RBTerm) parts[i].entryToObject(in);
		}
		return RBTuple.make(terms);
	}

	@Override
	public void objectToEntry(Object o, TupleOutput out) {
		RBTuple tup = (RBTuple) o;
		for (int i = 0; i < tup.getNumSubterms(); i++) {
			parts[i].objectToEntry(tup.getSubterm(i), out);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Binding<");
		for (int i = 0; i < parts.length; i++) {
			if (i>0)
				s.append(", ");
			s.append(parts[i]);
		}
		s.append(">");
		return s.toString();
	}

}

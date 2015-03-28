package tyRuBa.engine;

import java.io.PrintWriter;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.CompositeType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.ObjectTuple;

/**
 * @author kdvolder
 */
public class RBPair extends RBAbstractPair {

	/**
	 * Constructor for incomplete RBPair. For efficiency reasons only. Evil.
	 */
	public RBPair(RBTerm aCar) {
		super(aCar, null);
	}

	/**
	 * Constructor for RBPair.
	 */
	public RBPair(RBTerm aCar, RBTerm aCdr) {
		super(aCar, aCdr);
	}

	public static RBTerm make(RBTerm[] terms) {
		RBTerm t = FrontEnd.theEmptyList;
		for (int i = terms.length - 1; i >= 0; i--) {
			t = new RBPair(terms[i], t);
		}
		return t;
	}

	/** If proper list then Turn into an Object[] otherwise
	 *  just do as in super */
	public Object up() {
		try {
			int size = getNumSubterms();
			Object[] array = new Object[size];
			for (int i = 0; i < size; i++) {
				array[i] = getSubterm(i).up();
			}
			return array;
		} catch (ImproperListException e) {
			return super.up();
		}
	}
	
	@Override
	public void unparse(PrintWriter out) {
		out.print('[');
		getCar().unparse(out);
		RBTerm rest = this.getCdr(); 
		while (rest instanceof RBAbstractPair) {
			RBAbstractPair pair = (RBAbstractPair) rest;
			out.print(", ");
			pair.getCar().unparse(out);
			rest = pair.getCdr();
		}
		if (!rest.equals(FrontEnd.theEmptyList)) {
			out.print(" | ");
			rest.unparse(out);
		}
		out.print(']');
	}

	public String quotedToString() {
		return getCar().quotedToString() + getCdr().quotedToString();
	}

	public Type getType(TypeEnv env) throws TypeModeError {
		Type car,cdr;
		try {		
			car = getCar().getType(env);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, getCar());
		}
		try {		
			cdr = getCdr().getType(env);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, getCdr());
		}
		
		try {
			Type eltTp = Factory.makeTVar("el");
			Type list = Factory.makeListType(car).union(cdr);
			Factory.makeListType(eltTp).checkEqualTypes(list);
			return Factory.makeNonEmptyListType(eltTp);
		} catch (TypeModeError e) {
			throw new TypeModeError(e, this);
		}
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

    /**
     * @see tyRuBa.util.TwoLevelKey#getFirst()
     */
    public String getFirst() {
        return getCar().getFirst();
    }

    /**
     * @see tyRuBa.util.TwoLevelKey#getSecond()
     */
    public Object getSecond() {
        Object[] result = new Object[2];
        result[0] = getCar().getSecond();
        result[1] = getCdr();
        return ObjectTuple.make(result);
    }
}

package tyRuBa.engine;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;

/**
 * Base for all predicate implementations; it implements
 * how results are evaluated in a certain predicate mode 
 */
public abstract class AbstractImplementation extends RBComponent {
	
	private PredicateMode mode;
		
	public AbstractImplementation(PredicateMode mode) {
		this.mode = mode;
	}
		
	public PredicateMode getPredicateMode() {
		return mode;
	}

	public Mode getMode() {
		return getPredicateMode().getMode();
	}

	public BindingList getBindingList() {
		return getPredicateMode().getParamModes();
	}
	
	public PredicateIdentifier getPredId() {
		throw new Error("This should not happen");
	}
	
	public TupleType typecheck(PredInfoProvider predinfo) throws TypeModeError {
		throw new Error("This should not happen");
	}
	
	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context)
	throws TypeModeError {
		if (mode.equals(getPredicateMode())) {
			return this;
		} else {
			throw new Error("This should not happen");
		}
	}

	
	public String toString() {
		return "Implementation in mode: " + mode;
	}
	
	public abstract Compiled compile(CompilationContext c);

}

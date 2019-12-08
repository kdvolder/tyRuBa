package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RuleBase;
import tyRuBa.modes.Mode;
import tyRuBa.modes.TypeModeError;

public class SemiDetCompiledPredicateExpression extends SemiDetCompiled {

	final private RuleBase rules;
	final private RBTuple args;

	public SemiDetCompiledPredicateExpression(Mode mode, RuleBase rules, RBTuple args) {
		super(mode);
		this.rules = rules;
		this.args = args;	
	}

	final public Frame runSemiDet(final Object input, RBContext context) {
		try {
            RBTuple goal = (RBTuple)args.substitute((Frame)input);
            Frame result = compiledRules().runSemiDet(goal, context);
            if (((Frame)input).isEmpty()) {
//			PoormansProfiler.countEmptyFrameAppend++;
            	return result;
            } else if (result == null) {
            	return null;
            } else {
            	return ((Frame)input).append(result);
            }
        } catch (TypeModeError e) {
            return null;
        }
	}

	private SemiDetCompiled compiledRules() {
		return rules.getSemiDetCompiledRules();
	}
	
	public String toString() {
		return "SEMIDET PRED(" + args + ")";
	}

}

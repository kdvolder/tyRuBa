package tyRuBa.engine.compilation;

import tyRuBa.engine.Frame;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.modes.Mode;
import tyRuBa.modes.TypeModeError;

public class CompiledFact extends SemiDetCompiled {
	
	RBTuple args;

	public CompiledFact(RBTuple args) {
		super(Mode.makeSemidet());
		this.args = args;
	}

	public Frame runSemiDet(Object input, RBContext context) {
	    try {
    		RBTerm goal = (RBTerm) input;
    		// System.err.println("         Goal: " + goal);
    		// System.err.println("Checking Fact: " + this);
    		final Frame callFrame = new Frame();
    		// Rename all the variables in the goal to avoid name conflicts.
    		goal = (RBTuple) goal.instantiate(callFrame);
    		//System.err.println("     Unifying : " + goal);
    		//System.err.println("         with : " + args);
    		Frame fc = goal.unify(args, new Frame());
    		if (fc == null) {
    			return null;
    		} else {
    			//System.err.println("         resu : " + fc);
    			Frame result = callFrame.callResult(fc);
    			//System.err.println("    callFrame : " + callFrame);
    			//System.err.println("       result : " + result);
    			return result;
    		}
	    } catch (TypeModeError e) {
	        return null;
	    }
	}
	
	public String toString() {
		return "FACT(" + args + ")";
	}

}

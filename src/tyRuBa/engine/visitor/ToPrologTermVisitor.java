package tyRuBa.engine.visitor;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBJavaObjectCompoundTerm;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.TypeModeError;

public class ToPrologTermVisitor implements TermVisitor {

	private StringBuffer converted;

	public ToPrologTermVisitor(StringBuffer convertBuffer) {
		converted = convertBuffer;
	}

	public Object visit(RBCompoundTerm compoundTerm) {
		return null;
	}

	public Object visit(RBJavaObjectCompoundTerm javaTerm) {
		Object obj = javaTerm.getObject();
		if (javaTerm.equals(FrontEnd.theEmptyList))
			converted.append("[]");
		else if (obj instanceof String) {
			String str = (String) obj;
			converted.append("'");
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c=='\n')
					converted.append("\\n");
				else if (c=='\'')
					converted.append("\\'");
				else
					converted.append(c);
			}
			converted.append("'");
		}
		else if (obj instanceof Number) {
			converted.append(obj); 
			//TODO: I'm assuming that Prolog can parse Java number syntax
			//not sure this is actually always true.
		}
		else 
			throw new Error("Not implemented");
		return null;
	}
	
	public Object visit(RBIgnoredVariable ignoredVar) {
		converted.append("_");
		return null;
	}

	public Object visit(RBPair pair) throws TypeModeError {
		converted.append("[");
		pair.getCar().accept(this);
		RBTerm rest = pair.getCdr();
		while (rest instanceof RBPair) {
			converted.append(",");
			pair = (RBPair)rest;
			pair.getCar().accept(this);
			rest = pair.getCdr();
		}
		if (!rest.equals(FrontEnd.theEmptyList)) {
			converted.append("|");
			rest.accept(this);
		}
		converted.append("]");
		return null;
	}

	public Object visit(RBQuoted quoted) {
		throw new Error("Not Implemented");
	}

	public Object visit(RBTuple tuple) {
		throw new Error("Not implemented");
	}

	public Object visit(RBVariable var) {
		String tyRuBaName = var.name();
		converted.append("O"+tyRuBaName.substring(1));
		return null;
	}

	public Object visit(RBTemplateVar var) {
		String tyRuBaName = var.name();
		converted.append("I"+tyRuBaName.substring(1));
		return null;
	}

}

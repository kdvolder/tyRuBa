package tyRuBa.engine.visitor;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBJavaObjectCompoundTerm;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.TypeModeError;

public interface TermVisitor {

	Object visit(RBCompoundTerm compoundTerm) throws TypeModeError;

	Object visit(RBJavaObjectCompoundTerm compoundTerm);
	
	Object visit(RBIgnoredVariable ignoredVar);

	Object visit(RBPair pair) throws TypeModeError;

	Object visit(RBQuoted quoted) throws TypeModeError;
	
	Object visit(RBTuple tuple) throws TypeModeError;

	Object visit(RBVariable var) throws TypeModeError;

	Object visit(RBTemplateVar var) throws TypeModeError;    
}

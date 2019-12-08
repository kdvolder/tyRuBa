package tyRuBa.engine.visitor;

import java.util.Collection;
import java.util.HashSet;

import tyRuBa.engine.RBCountAll;
import tyRuBa.engine.RBDisjunction;
import tyRuBa.engine.RBExistsQuantifier;
import tyRuBa.engine.RBFindAll;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBNotFilter;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTestFilter;
import tyRuBa.engine.RBUniqueQuantifier;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.TypeModeError;

/**
 * This visitor visits RBExpression and collects all variables that will
 * become bound after evaluation of the expression.
 */
public class CollectVarsVisitor extends AbstractCollectVarsVisitor {

	public CollectVarsVisitor(Collection vars) {
		super(vars, null);
	}
	
	public CollectVarsVisitor() {
		super(new HashSet(), null);
	}

	public Object visit(RBDisjunction disjunction) {
		Collection oldVars = getVars();
		Collection intersection = null;
		for (int i = 0; i < disjunction.getNumSubexps(); i++) {
			Collection next = disjunction.getSubexp(i).getVariables();
			if (intersection==null)
				intersection = next;
			else
				intersection.retainAll(next);
		}
		if (intersection != null) {
			oldVars.addAll(intersection);
		}
		return null;
	}

	public Object visit(RBExistsQuantifier exists) throws TypeModeError {
		return exists.getExp().accept(this);
	}

	public Object visit(RBFindAll findAll) throws TypeModeError {
		return findAll.getResult().accept(this);
	}

	public Object visit(RBCountAll count) throws TypeModeError {
		return count.getResult().accept(this);
	}

	public Object visit(RBNotFilter notFilter) {
		return null;
	}

	public Object visit(RBTestFilter testFilter) {
		return null;
	}

	public Object visit(RBUniqueQuantifier unique) throws TypeModeError {
		return unique.getExp().accept(this);
	}

	public Object visit(RBVariable var) {
		getVars().add(var);
		return null;
	}
	
	public Object visit(RBIgnoredVariable ignoredVar) {
		getVars().add(ignoredVar);
		return null;
	}

	public Object visit(RBTemplateVar templVar) {
		return null;
	}
    
}

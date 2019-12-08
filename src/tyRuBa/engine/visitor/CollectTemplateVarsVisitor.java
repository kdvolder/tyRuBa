package tyRuBa.engine.visitor;

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

public class CollectTemplateVarsVisitor extends AbstractCollectVarsVisitor {

	public CollectTemplateVarsVisitor() {
		super(new HashSet(), null);
	}

	public Object visit(RBDisjunction disjunction) throws TypeModeError {
		for (int i = 0; i < disjunction.getNumSubexps(); i++) {
			disjunction.getSubexp(i).accept(this);
		}
		return null;
	}

	public Object visit(RBExistsQuantifier exists) throws TypeModeError {
		exists.getExp().accept(this);
		return null;
	}

	public Object visit(RBFindAll findAll) throws TypeModeError {
		findAll.getQuery().accept(this);
		findAll.getResult().accept(this);
		findAll.getExtract().accept(this);
		return null;
	}

	public Object visit(RBCountAll count) throws TypeModeError {
		count.getQuery().accept(this);
		count.getResult().accept(this);
		count.getExtract().accept(this);
		return null;
	}

	public Object visit(RBNotFilter notFilter) throws TypeModeError {
		notFilter.getNegatedQuery().accept(this);
		return null;
	}

	public Object visit(RBTestFilter testFilter) throws TypeModeError {
		testFilter.getQuery().accept(this);
		return null;
	}

	public Object visit(RBUniqueQuantifier unique) throws TypeModeError {
		unique.getExp().accept(this);
		return null;
	}

	public Object visit(RBIgnoredVariable ignoredVar) {
		return null;
	}

	public Object visit(RBVariable var) {
		return null;
	}

	public Object visit(RBTemplateVar var) {
		vars.add(var);
		return null;
	}

}

package tyRuBa.engine.visitor;

import tyRuBa.engine.RBConjunction;
import tyRuBa.engine.RBCountAll;
import tyRuBa.engine.RBDisjunction;
import tyRuBa.engine.RBExistsQuantifier;
import tyRuBa.engine.RBFindAll;
import tyRuBa.engine.RBModeSwitchExpression;
import tyRuBa.engine.RBNotFilter;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RBTestFilter;
import tyRuBa.engine.RBUniqueQuantifier;
import tyRuBa.modes.TypeModeError;

public interface ExpressionVisitor {

	public Object visit(RBConjunction conjunction) throws TypeModeError;

	public Object visit(RBDisjunction disjunction) throws TypeModeError;

	public Object visit(RBExistsQuantifier exists) throws TypeModeError;

	public Object visit(RBFindAll findAll) throws TypeModeError;

	public Object visit(RBCountAll count) throws TypeModeError;

	public Object visit(RBModeSwitchExpression modeSwitch) throws TypeModeError;

	public Object visit(RBNotFilter notFilter)throws TypeModeError;

	public Object visit(RBPredicateExpression predExp)throws TypeModeError;

	public Object visit(RBTestFilter testFilter) throws TypeModeError;

	public Object visit(RBUniqueQuantifier unique) throws TypeModeError;

}

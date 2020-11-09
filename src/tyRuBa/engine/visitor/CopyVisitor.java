package tyRuBa.engine.visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBConjunction;
import tyRuBa.engine.RBCountAll;
import tyRuBa.engine.RBDisjunction;
import tyRuBa.engine.RBExistsQuantifier;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBFindAll;
import tyRuBa.engine.RBIgnoredVariable;
import tyRuBa.engine.RBJavaObjectCompoundTerm;
import tyRuBa.engine.RBModeSwitchExpression;
import tyRuBa.engine.RBNotFilter;
import tyRuBa.engine.RBPair;
import tyRuBa.engine.RBPredicateExpression;
import tyRuBa.engine.RBQuoted;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTestFilter;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.RBUniqueQuantifier;
import tyRuBa.engine.RBVariable;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.ModeCase;
import tyRuBa.modes.TypeModeError;

public class CopyVisitor implements ExpressionVisitor, TermVisitor {

	@Override
	public Object visit(RBConjunction conjunction) throws TypeModeError {
		RBConjunction result = new RBConjunction();
		for (int i = 0; i < conjunction.getNumSubexps(); i++) {
			result.addSubexp(
				(RBExpression) conjunction.getSubexp(i).accept(this));
		}
		return result;
	}

	public Object visit(RBDisjunction disjunction) throws TypeModeError {
		RBDisjunction result = new RBDisjunction();
		for (int i = 0; i < disjunction.getNumSubexps(); i++) {
			result.addSubexp(
				(RBExpression) disjunction.getSubexp(i).accept(this));
		}
		return result;
	}

	public Object visit(RBExistsQuantifier exists) throws TypeModeError {
		RBExpression exp = (RBExpression) exists.getExp().accept(this);
		Collection vars = new HashSet();
		for (int i = 0; i < exists.getNumVars(); i++) {
			vars.add(exists.getVarAt(i).accept(this));
		}
		return new RBExistsQuantifier(vars, exp);
	}

	public Object visit(RBFindAll findAll) throws TypeModeError {
		RBExpression query = (RBExpression) findAll.getQuery().accept(this);
		RBTerm extract = (RBTerm) findAll.getExtract().accept(this);
		RBTerm result = (RBTerm) findAll.getResult().accept(this);
		return new RBFindAll(query, extract, result);
	}

	public Object visit(RBCountAll count) throws TypeModeError {
		RBExpression query = (RBExpression) count.getQuery().accept(this);
		RBTerm extract = (RBTerm) count.getExtract().accept(this);
		RBTerm result = (RBTerm) count.getResult().accept(this);
		return new RBCountAll(query, extract, result);
	}

	public Object visit(RBModeSwitchExpression modeSwitch) throws TypeModeError {
		int num = modeSwitch.getNumModeCases();
		List<ModeCase> modeCases = new ArrayList<>();
		for (int i = 0; i < num; i++) {
			ModeCase cs = modeSwitch.getModeCaseAt(i);
			RBExpression exp = (RBExpression)cs.getExp();
			RBExpression copyExp = (RBExpression) exp.accept(this);
			ModeCase csCopy = new ModeCase(new HashSet<>(cs.getBoundVars()),copyExp);
			modeCases.add(csCopy);
		}
		RBExpression dflt = modeSwitch.getDefaultExp();
		return new RBModeSwitchExpression(modeCases, dflt == null ? null : (RBExpression)dflt.accept(this));
	}

	public Object visit(RBNotFilter notFilter) throws TypeModeError {
		RBExpression negatedQuery = 
			(RBExpression) notFilter.getNegatedQuery().accept(this);
		return new RBNotFilter(negatedQuery);
	}
	
	public Object visit(RBPredicateExpression predExp) throws TypeModeError {
		return predExp.withNewArgs((RBTuple)predExp.getArgs().accept(this));
	}

	public Object visit(RBTestFilter testFilter) throws TypeModeError {
		RBExpression testQuery = 
			(RBExpression) testFilter.getQuery().accept(this);
		return new RBTestFilter(testQuery);
	}

	@Override
	public Object visit(RBUniqueQuantifier unique) throws TypeModeError {
		return new RBUniqueQuantifier(
				Arrays.asList(unique.getQuantifiedVars()), 
				(RBExpression) unique.getExp().accept(this)
		);
	}

	public Object visit(RBJavaObjectCompoundTerm compoundTerm) {
		return compoundTerm;
	}
	
	public Object visit(RBCompoundTerm compoundTerm) throws TypeModeError {
		ConstructorType typeConst = compoundTerm.getConstructorType();
		return typeConst.apply(
			(RBTerm) compoundTerm.getArg().accept(this));
//		PredicateIdentifier pred = compoundTerm.getPredId();
//		return RBCompoundTerm.makeForVisitor(pred, (RBTerm)compoundTerm.getArgsForVisitor().accept(this));
	}

	public Object visit(RBTuple tuple) throws TypeModeError {
		RBTerm[] subterms = new RBTerm[tuple.getNumSubterms()];
		for (int i = 0; i < subterms.length; i++) {
			subterms[i] = (RBTerm) tuple.getSubterm(i).accept(this);
		}
		return RBTuple.make(subterms);
	}

	public Object visit(RBIgnoredVariable ignoredVar) {
		return ignoredVar;
	}

	public Object visit(RBPair pair) throws TypeModeError {
		RBPair head = new RBPair((RBTerm)pair.getCar().accept(this));
		
		RBPair next;
		RBPair prev = head;
		
		RBTerm cdr = (RBTerm)pair.getCdr();
		
		while(cdr instanceof RBPair) {
			pair = (RBPair)cdr;
			next = new RBPair((RBTerm)pair.getCar().accept(this));
			prev.setCdr(next);
			prev = next;
			cdr = pair.getCdr();
		}
		
		prev.setCdr((RBTerm)cdr.accept(this));
		
		return head;
	}

	public Object visit(RBQuoted quoted) throws TypeModeError {
		return new RBQuoted(
			(RBTerm)quoted.getQuotedParts().accept(this));
	}

	@Override
	public Object visit(RBVariable var) throws TypeModeError {
		return var;
	}

	@Override
	public Object visit(RBTemplateVar var) throws TypeModeError {
		return var;
	}
}

package tyRuBa.engine;

import java.util.Collection;

import junit.framework.Assert;

import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.visitor.CollectFreeVarsVisitor;
import tyRuBa.engine.visitor.CollectTemplateVarsVisitor;
import tyRuBa.engine.visitor.CollectVarsVisitor;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.engine.visitor.SubstituteVisitor;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.PreparedQuery;

public abstract class RBExpression implements Cloneable {

    private Mode mode = null;
    private ModeCheckContext newContext = null;
    
	abstract public Compiled compile(CompilationContext c);

	PreparedQuery prepareForRunning(QueryEngine engine, boolean typeCheck)
		throws TypeModeError, ParseException {
		engine.frontend().autoUpdateBuckets();
		RBExpression converted = convertToNormalForm();
		TypeEnv resultEnv = null;
		if (typeCheck) 
			resultEnv = converted.typecheck(engine.rulebase(), Factory.makeTypeEnv());
		else
			resultEnv = converted.fakecheck(engine.rulebase(), Factory.makeTypeEnv());
		RBExpression result =
			converted.convertToMode(Factory.makeModeCheckContext(engine.rulebase()));
		if (result.getMode() instanceof ErrorMode) {
			throw new TypeModeError(
				this + " cannot be converted to any declared mode\n" +
					"   " + result.getMode());
		} else if (!RuleBase.silent) {
			if (typeCheck)
				System.err.println("inferred types: " + resultEnv);
			System.err.println("converted to Mode: " + result);
		}
		//Compiled compExp = result.getExp().compile(new CompilationContext());
		return new PreparedQuery(engine, result, resultEnv);
	}

	/** 
	 * A quick hack used to execute queries without type checking. 
	 * This performs similar to the typecheck method but instead of checking
	 * types it merely retrieves the variables from the query and associates
	 * the type Object with them.
	 */
	private TypeEnv fakecheck(ModedRuleBaseIndex index, TypeEnv env) {
		Type object = TypeConstructor.theAny.apply(Factory.makeTupleType(), false);
		Collection vars = getTemplateVariables();
		for (Object var : vars) {
			env.put(var, object);
		}
		return env;
	}

	/**
	 * Returns all the template variables (input for prepared queries)
	 * found in this expression.
	 */
	private Collection getTemplateVariables() {
		CollectTemplateVarsVisitor visitor = new CollectTemplateVarsVisitor();
		this.accept(visitor);
		Collection vars = visitor.getVars();
		return vars;
	}

	/**
	 * Returns a Collection of variables bound by this query expression.
	 */
	public final Collection getVariables() {
		CollectVarsVisitor visitor = new CollectVarsVisitor();
		this.accept(visitor);
		Collection vars = visitor.getVars();
		vars.remove(RBIgnoredVariable.the);
		return vars;
	}
	
	/**
	 * Returns a Cpllection of variables bound by this query expression that do
	 * not become bound in the context.
	 */
	public final Collection getFreeVariables(ModeCheckContext context) {
		CollectFreeVarsVisitor visitor = new CollectFreeVarsVisitor(context);
		this.accept(visitor);
		return visitor.getVars();
	}
	
	public abstract TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv)
	throws TypeModeError;

	public final RBExpression convertToMode(ModeCheckContext context)
	throws TypeModeError {
		return convertToMode(context, true);
	}

	public abstract RBExpression convertToMode(ModeCheckContext context,
	boolean rearrange) throws TypeModeError;

	
	public RBExpression convertToNormalForm() {
		return convertToNormalForm(false);
	}
	
	public RBExpression convertToNormalForm(boolean negate) {
		RBExpression result;
		if (negate) {
			result = new RBNotFilter(this);
		} else {
			result = this;
		}
		return result;
	}
	
	public RBExpression crossMultiply(RBExpression other) {
		if (other instanceof RBCompoundExpression)
			return other.crossMultiply(this);
		return FrontEnd.makeAnd(this, other);
	}

	public abstract Object accept(ExpressionVisitor v);
	
	public RBExpression substitute(Frame frame) {
		SubstituteVisitor visitor = new SubstituteVisitor(frame);
		return (RBExpression) accept(visitor);
	}

	public RBExpression addExistsQuantifier(RBVariable[] newVars, boolean negate) {
		RBExistsQuantifier exists = new RBExistsQuantifier(newVars, this);
		if (negate) {
			return new RBNotFilter(exists);
		} else {
			return exists;
		}
	}

    public RBExpression makeModed(Mode mode, ModeCheckContext context) {
        try {
            RBExpression clone = (RBExpression) this.clone();
            clone.setMode(mode,context);
            return clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new Error("Should not happen");
        }     
    }

    /**
     * This method should only be called while initializing a Moded expression.
     */
    private void setMode(Mode mode, ModeCheckContext context) {
 //       Assert.assertNull(this.mode);
        this.mode = mode;
        this.newContext = context;
    }

    /** Return true if this moded expression returns less result than other*/
    public boolean isBetterThan(RBExpression other) {
        return getMode().isBetterThan(other.getMode());
    }

    protected Mode getMode() {
        return mode;
    }

    public ModeCheckContext getNewContext() {
        Assert.assertNotNull(newContext);
        return newContext;
    }

//    public String toString() {
//        return "ModedExpression: " + getExp() + "\n with mode: " + getMode() 
//            + "\n and context: " + getNewContext();
//    }

}

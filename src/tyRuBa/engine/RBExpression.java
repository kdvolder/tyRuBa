package tyRuBa.engine;

import java.util.Collection;
import java.util.Stack;

import junit.framework.Assert;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.visitor.CollectFreeVarsVisitor;
import tyRuBa.engine.visitor.CollectTemplateVarsVisitor;
import tyRuBa.engine.visitor.CollectVarsVisitor;
import tyRuBa.engine.visitor.CopyVisitor;
import tyRuBa.engine.visitor.ExpressionVisitor;
import tyRuBa.engine.visitor.SubstituteVisitor;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.ErrorMode;
import tyRuBa.modes.Factory;
import tyRuBa.modes.JavaConstructorType;
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
		try {
            CollectTemplateVarsVisitor visitor = new CollectTemplateVarsVisitor();
            this.accept(visitor);
            Collection vars = visitor.getVars();
            return vars;
        } catch (TypeModeError e) {
            throw new IllegalStateException(e);
        }
	}

	/**
	 * Returns a Collection of variables bound by this query expression.
	 */
	public final Collection getVariables() {
		try {
            CollectVarsVisitor visitor = new CollectVarsVisitor();
            this.accept(visitor);
            Collection vars = visitor.getVars();
            vars.remove(RBIgnoredVariable.the);
            return vars;
        } catch (TypeModeError e) {
            throw new IllegalStateException(e);
        }
	}
	
	/**
	 * Returns a Cpllection of variables bound by this query expression that do
	 * not become bound in the context.
	 */
	public final Collection getFreeVariables(ModeCheckContext context) {
        try {
    		CollectFreeVarsVisitor visitor = new CollectFreeVarsVisitor(context);
    		this.accept(visitor);
    		return visitor.getVars();
        } catch (TypeModeError e) {
            throw new IllegalStateException(e);
        }
	}
	
	public abstract TypeEnv typecheck(PredInfoProvider predinfo, TypeEnv startEnv)
	throws TypeModeError;

	public final RBExpression convertToMode(ModeCheckContext context)
	throws TypeModeError {
		return convertToMode(context, true);
	}

	public abstract RBExpression convertToMode(ModeCheckContext context,
	boolean rearrange) throws TypeModeError;

	
	public RBExpression convertToNormalForm() throws TypeModeError {
		return this.eliminateVariableCasts().convertToNormalForm(false);
	}
	
	protected RBExpression eliminateVariableCasts() throws TypeModeError {
		return (RBExpression) this.accept(new CopyVisitor() {

			RBConjunction conjunction = null;
			RBPredicateExpression predExp;

			@Override
			public Object visit(RBPredicateExpression predExp) throws TypeModeError {
				if (this.predExp!=null) {
					throw new IllegalStateException("Bug!");
				}
				try {
					conjunction = new RBConjunction();
					conjunction.addSubexp((RBExpression)super.visit(predExp));
					if (conjunction.getNumSubexps()==1) {
						return conjunction.getSubexp(0);
					}
					return conjunction;
				} finally {
					this.conjunction = null;
					this.predExp = null;
				}
			}
			
			@Override
			public Object visit(RBCompoundTerm compoundTerm) throws TypeModeError {
				if (compoundTerm instanceof RBGenericCompoundTerm) {
					RBGenericCompoundTerm varCast = (RBGenericCompoundTerm) compoundTerm;
					if (varCast.getNumArgs()==1) {
						ConstructorType constructor = varCast.getConstructorType();
						if (constructor instanceof JavaConstructorType) {
							JavaConstructorType javaConstructor = (JavaConstructorType) constructor;
							RBTerm _var = varCast.getArg(0);
							if (_var instanceof RBVariable) {
								RBVariable var = (RBVariable) _var;
								String name = javaConstructor.getJavaClass().getName();
								conjunction.addSubexp(new RBPredicateExpression(name, new RBTuple(var)));
								return var;
							}
						}
						
					}
				}
				return super.visit(compoundTerm);
			}
		});
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

	public abstract Object accept(ExpressionVisitor v) throws TypeModeError;
	
	public RBExpression substitute(Frame frame) throws TypeModeError {
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

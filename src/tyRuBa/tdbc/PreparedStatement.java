/*
 * Created on Feb 24, 2004
 */
package tyRuBa.tdbc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBSubstitutable;
import tyRuBa.engine.RBTemplateVar;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;

public abstract class PreparedStatement {

	private TypeEnv      orgTEnv;
	private TypeEnv      workTEnv;
	   // TypeEnv resulted from type checking this expression.
	   // This is needed to verify correct type of "put!"
	
	protected Frame      putMap = new Frame();
	   // Keeps the values for template variables set by the user with put
	
	private Set		     mustPut = null;
	   // Set of names of the template varaibles not yet put. 
	   // This set must be empty by the time
	   // the preparedQuery gets executed.
	   // Note: the variable should never actually hold an empty HashSet.
	   // it is assumed that in this case the variable will be set to null.

    protected QueryEngine engine;
	protected FrontEnd frontend;

    public PreparedStatement(QueryEngine engine, TypeEnv tEnv) {
        this.engine = engine;
        this.frontend = engine.frontend();
		this.orgTEnv = tEnv;
		initialize(tEnv);
	}

	private void initialize(TypeEnv tEnv) {
		this.workTEnv = null;
		for (Iterator iter = tEnv.keySet().iterator(); iter.hasNext();) {
			RBSubstitutable var = (RBSubstitutable) iter.next();
			if (var instanceof RBTemplateVar) {
				if (mustPut==null)
					mustPut = new HashSet();
				mustPut.add(var.name());
			}
		}
	}
	
    protected void checkReadyToRun() throws TyrubaException {
		if (!readyToRun())
			throw new TyrubaException("Some input variables left unbound: "+mustPut);
	}

	public boolean readyToRun() {
		return (mustPut==null);
	}

	/** 
	 * May throw a TyRuBaException if the templateVar is unknown or the
	 * value provided is of a type not acceptable for the context where the
	 * templateVar occurs.
	 */
    public void put(String templateVar, Object value ) throws TyrubaException {
        try {
            RBTerm term = engine.makeJavaObject(value);
            checkTermType(templateVar,term);
            if (mustPut!=null) {
                mustPut.remove(templateVar);
                if (mustPut.isEmpty())
                    mustPut = null;
            }
            putMap.put(new RBTemplateVar(templateVar),term);
        } catch (TypeModeError e) {
            throw new TyrubaException(e);
        }
	}

	/**
	 * Simlar to put(String var, Object value) but pretends that
	 * the value is of a more general type than the actual object.
	 * For example, if value was a "Method" it could also be
	 * considered to be of type "Member" (assuming that Member is
	 * a super type of Method).
	 * 
	 * @param string
	 * @param m
	 * @param type
	 * @throws TyrubaException 
	 */
    public void put(String templateVar, Object value, Type assumedType) throws TyrubaException {
        try {
            RBTerm term = engine.makeJavaObject(value);
            checkTermType(assumedType,term);
            checkVarType(templateVar,assumedType);
            if (mustPut!=null) {
                mustPut.remove(templateVar);
                if (mustPut.isEmpty())
                    mustPut = null;
            }
            putMap.put(new RBTemplateVar(templateVar),term);
        } catch (TypeModeError e) {
            throw new TyrubaException(e);
        }
    }

	private void checkTermType(Type assumedType, RBTerm term) throws TyrubaException {
		if (workTEnv==null) workTEnv = (TypeEnv) orgTEnv.clone();
		try {
			assumedType.checkEqualTypes(term.getType(workTEnv));
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}
	}

	private void checkVarType(String templateVarName, Type termType) throws TyrubaException {
		if (workTEnv==null) workTEnv = (TypeEnv) orgTEnv.clone();
		RBTemplateVar var = new RBTemplateVar(templateVarName);
		Type expected = workTEnv.basicGet(var);
		if (expected==null)
			throw new TyrubaException("Variable "+templateVarName+" is unknown.");
		if (!expected.hasOverlapWith(termType)) {
			throw new TyrubaException("Type "+termType+" not compatible with variable type "+expected);
		}
	}
	
	private void checkTermType(String templateVarName, RBTerm term) throws TyrubaException {
		if (workTEnv==null) workTEnv = (TypeEnv) orgTEnv.clone();
		RBTemplateVar var = new RBTemplateVar(templateVarName);
		Type expected = workTEnv.basicGet(var);
		if (expected==null)
			throw new TyrubaException("Variable "+templateVarName+" is unknown.");
		try {
			expected.checkEqualTypes(term.getType(workTEnv));
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		}
	}

//	private void checkVarType(String templateVarName, Object value) throws TyrubaException {
//		RBTemplateVar var = new RBTemplateVar(templateVarName);
//		Type expected = tEnv.basicGet(var);
//		if (expected==null)
//			throw new TyrubaException("Trying to put an unknown variable: "+templateVarName);
//		
//		try {
//		    Class expectedClass = expected.javaEquivalent();
//		    if (expectedClass==null) {
//		        throw new TyrubaException("There is no Java equivalent for tyRuBa type "+expected);
//		    }
//		    if (!expectedClass.isAssignableFrom(value.getClass())) {
//		        throw new TyrubaException("Value: "+value+" of class "+ value.getClass().getName() +" expected "+expectedClass.getName());
//		    }
//		} catch (TypeModeError e) {
//		    e.printStackTrace();
//		    throw new TyrubaException(e.getMessage());
//		}
//		
//	}

	public void put(String templateVar, int value ) throws TyrubaException {
		put(templateVar,new Integer(value));
	}

	public void put(String templateVar, long value ) throws TyrubaException {
		put(templateVar,new Long(value));
	}

	public void put(String templateVar, float value ) throws TyrubaException {
		put(templateVar,new Float(value));
	}

	public void put(String templateVar, boolean value ) throws TyrubaException {
		put(templateVar,new Boolean(value));
	}

    public QueryEngine getEngine() {
        return engine;
    }
}

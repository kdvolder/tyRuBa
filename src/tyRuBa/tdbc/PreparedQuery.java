/*
 * Created on Feb 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package tyRuBa.tdbc;

import java.util.Collection;
import java.util.Iterator;

import tyRuBa.engine.Frame;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBVariable;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.DelayedElementSource;
import tyRuBa.util.ElementSource;

/**
 * @author dsjanzen
 */
public class PreparedQuery extends PreparedStatement{

	protected static final int CONJUNCTION_PRIORITY = 2;

	private Compiled     compiled;
	private String[]     outputVars;
	private RBExpression exp;

	/**
	 * This method is not intended for public use. It used by the TyRuBa engine
	 * to create instances of PreparedQuery.
	 * 
	 * @param engine
output	 * @param preparedExp
	 * @param tEnv
	 */
	public PreparedQuery(QueryEngine engine,RBExpression preparedExp,TypeEnv tEnv) {
		super(engine,tEnv);
		this.exp = preparedExp;
		Collection vars = preparedExp.getVariables();
		outputVars = new String[vars.size()];
		int i=0;
		for (Iterator iter = vars.iterator(); iter.hasNext();) {
			RBVariable var = (RBVariable) iter.next();
			outputVars[i++] = var.name();
		}
		//TODO: This should be lazy:
		this.compiled = preparedExp.compile(new CompilationContext());
	}

	/**
	 * Return the names of the variables that this query will bind.
	 * 
	 * @return an array of Strings containing the names of the variables.
	 */
	public String[] getOutputVariables() {
		return outputVars;
	}

	public ResultSet executeQuery() throws TyrubaException {
		checkReadyToRun();
		try {
			return new ResultSet(start());
		} catch (TypeModeError e) {
			throw new TyrubaException(e);
		} catch (ParseException e) {
			throw new TyrubaException(e);
		}
	}

	public ElementSource start() throws TypeModeError, ParseException {
		frontend.autoUpdateBuckets();
		synchronized (frontend) {
			final Frame localPutMap = (Frame) putMap.clone();
			//TODO: logger.logQuery(e);
			// CAUTION! 
			// In very rare occasions, there is potential problem that
			// something like outdaing buckets or saving factbase comes
			// in between the end of runable.start() and synchronizeOn.
			// Since not all elementsources are 100% lazy the start()
			// invocation may actually have
			// started working on the first element. If the engine state
			// is altered in between there is thus
			// a potential inconsistency! 
			// So we must guard against this problem by making sure
			// the source is fully lazy by using a DelayedElementSource here.
			frontend.getSynchPolicy().newSource();
			ElementSource result = new DelayedElementSource() {

				protected ElementSource produce() {
					//System.err.println("[INFO] - QueryEngine - Starting Query");
					return compiled.start(localPutMap);
				}

				protected String produceString() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			frontend.getSynchPolicy().sourceDone();
			return result.synchronizeOn(frontend);
			//System.err.println("[INFO] - QueryEngine - Done Query");
		}
	}

}

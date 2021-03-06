/*
 * Created on Jun 25, 2003
 */
package tyRuBa.modes;

import java.util.Collection;

import tyRuBa.engine.RBExpression;
import tyRuBa.engine.RBVariable;

public class ModeCase {
	private Collection<RBVariable> boundVars;
	private RBExpression exp;
		
	public ModeCase(Collection boundVars, RBExpression exp) {
		this.boundVars = boundVars;
		this.exp = exp;
	}
		
	public RBExpression getExp() {
		return exp;
	}
		
	public Collection<RBVariable> getBoundVars() {
		return boundVars;
	}
	
	public String toString() {
		String varString = getBoundVars().toString();
		return "BOUND " + varString.substring(1, varString.length() - 1)
			+ " : " + getExp();
	}
}
	

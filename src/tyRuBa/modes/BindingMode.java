/*****************************************************************\
 * File:        BindingMode.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import tyRuBa.engine.RBTerm;

abstract public class BindingMode {
	
	public abstract boolean isBound();
	public abstract boolean isFree();

	public abstract boolean satisfyBinding(BindingMode mode);
	public abstract String getBFString();
	
	public boolean usePartialKeyExtraction() {
		return false;
	}
	public abstract RBTerm extractPartialKey(RBTerm subterm);
	
	public abstract Type partialKeyType(Type type);
	
}

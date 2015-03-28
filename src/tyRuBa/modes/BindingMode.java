/*****************************************************************\
 * File:        BindingMode.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import tyRuBa.engine.RBTerm;
import annotations.Feature;

abstract public class BindingMode {
	
	public abstract boolean isBound();
	public abstract boolean isFree();

	public abstract boolean satisfyBinding(BindingMode mode);
	public abstract String getBFString();
	
	@Feature(names="./partialKey")
	public boolean usePartialKeyExtraction() {
		return false;
	}
	@Feature(names="./partialKey")
	public abstract RBTerm extractPartialKey(RBTerm subterm);
	
	@Feature(names="./partialKey")
	public abstract Type partialKeyType(Type type);
	
}

/*****************************************************************\
 * File:        Bound.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import tyRuBa.engine.RBTerm;

public class Bound extends BindingMode {
	
	static public Bound the = new Bound();

	private Bound() {}

	public int hashCode() {
		return this.getClass().hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof Bound;
	}

	public String toString() {
		return "B";
	}

	public boolean satisfyBinding(BindingMode mode) {
		return true;
	}

	public boolean isBound() {
		return true;
	}
	public boolean isFree() {
		return false;
	}

	public String getBFString() {
		return "B";
	}

	@Override
	public RBTerm extractPartialKey(RBTerm term) {
		return term;
	}

	@Override
	public Type partialKeyType(Type type) {
		return type;
	}
	
}

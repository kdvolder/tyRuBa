package tyRuBa.modes;

import tyRuBa.engine.RBTerm;

/**
 * The binding mode of a term that is neither fully bound
 * nor fully unbound but without any additional information
 * about how/what parts are bound.
 * 
 * @author kdvolder
 */
public class PartiallyBound extends BindingMode {
	
	static public PartiallyBound the = new PartiallyBound();

	protected PartiallyBound() {}

	public int hashCode() {
		return this.getClass().hashCode();
	}

	public boolean equals(Object other) {
		return other instanceof PartiallyBound;
	}

	public String toString() {
		return "BF";
	}

	/** check that this binding satisfied the binding mode */
	public boolean satisfyBinding(BindingMode mode) {
		return mode instanceof Free;
	}

	public boolean isBound() {
		return false;
	}
	public boolean isFree() {
		return false;
	}

	public String getBFString() {
		return "b";
	}

	@Override
	public RBTerm extractPartialKey(RBTerm term) {
		throw new Error("This shouldn't happen: partial key extraction not supported");
	}

	@Override
	public Type partialKeyType(Type type) {
		throw new Error("This shouldn't happen: partial key extraction not supported");
	}
}

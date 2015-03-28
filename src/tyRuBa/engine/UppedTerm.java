package tyRuBa.engine;

import java.io.Serializable;

/** 
 * This is wrapper for RBTerms that get passed to Java functions. 
 * Is used for RBTerm objects that don't really know how to map
 * themselves onto a Java equivalent.  At least this preserves
 * the x.up().down() = x  invariant.
*/

public class UppedTerm implements Serializable {
	private static final long serialVersionUID = 1;
	
	RBTerm term;

	public UppedTerm(RBTerm t) {
		term = t;
	}

	public RBTerm down() {
		return term;
	}

	public String toString() {
		return term.toString();
	}
	
	/**
	 * Two upped terms are equal if they reference the same RBTerm
	 * @author lmarkle
	 */
	public boolean equals(Object o) {
		if(o instanceof UppedTerm) {
			UppedTerm t = (UppedTerm)o;
			return (t.term.equals(term));
		}
		return false;
	}

	/** 
	 * Two upped terms have the same hashcode if their RBTerms have the same hashCode.
	 * @author lmarkle
	 */
	public int hashCode() {
		return term.hashCode();
	}
}

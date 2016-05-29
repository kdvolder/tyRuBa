/*****************************************************************\
 * File:        BindingList.java
 * Author:      TyRuBa
 * Meta author: Kris De Volder <kdvolder@cs.ubc.ca>
\*****************************************************************/
package tyRuBa.modes;

import java.util.ArrayList;

/** 
 * A BoundTuple is the BindingMode for a Tuple
 */
public class BindingList {
	
	private ArrayList<BindingMode> parts;
	
	private int partialKeySize = -1; // cache value for getPartialKeySize

	public BindingList() {
		parts = new ArrayList<BindingMode>();
	}
	
	public BindingList(BindingMode bm) {
		this();
		parts.add(bm);
	}

	public int hashCode() {
		final int size = size();
		int hash = getClass().hashCode();
		for (int i = 0; i < size; i++)
			hash = hash * 19 + get(i).hashCode();
		return hash;
	}

	public boolean equals(Object other) {
		if (other instanceof BindingList) {
			BindingList cother = (BindingList) other;
			if (this.size() != cother.size()) {
				return false;
			} else {
				for (int i = 0; i < size(); i++) {
					if (!(get(i).equals(cother.get(i))))
						return false;
				}
				return true;
			}
		} else {
			return false;
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer("(");
		int size = this.size();
		for (int i = 0; i < size; i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append(this.get(i).toString());
		}
		result.append(")");
		return result.toString();
	}
	
	public String getBFString() {
		StringBuffer result = new StringBuffer();
		int size = this.size();
		for(int i = 0; i < size; i++) {
			result.append(get(i).getBFString());
		}
		return result.toString();
	}

	/** add newPart to the end of the list */
	public void add(BindingMode newPart) {
		parts.add(newPart);
		partialKeySize = -1;
	}

	public BindingMode get(int i) {
		return parts.get(i);
	}

	public int size() {
		return parts.size();
	}

	public boolean hasFree() {
		for (int i = 0; i < size(); i++) 
			if (!get(i).isBound()) return true;
		return false;
	}

	public boolean isAllBound() {
		for (int i = 0; i < size(); i++) {
			if (!get(i).isBound()) {
				return false;
			}
		}
		return true;
	}

	public int getNumBound() {
		int result = 0; 
		for (int i = 0; i < size(); i++) {
			if (get(i).isBound()) {
				result++;
			}
		}
		return result;
	}

    public int getNumFree() {
        return size() - getNumBound();
    }

	public int[] getBoundIndexes() {
		int[] indexes = new int[getNumBound()];
		int b = 0;
		for (int i = 0; i < size(); i++) {
			if (get(i).isBound())
				indexes[b++]=i;
		}
		return indexes;
	}

	public int[] getFreeIndexes() {
		int[] indexes = new int[getNumFree()];
		int b = 0;
		for (int i = 0; i < size(); i++) {
			if (get(i).isFree())
				indexes[b++]=i;
		}
		return indexes;
	}

	public BindingList approximateBFWithF() {
		BindingList result = new BindingList();
		for (int i = 0; i < size(); i++) {
			BindingMode current = get(i);
			result.add(current.isBound()?Factory.makeBound():Factory.makeFree());
		}
		return result;
	}

	/** 
	 * return true if this BindingMode has no Free at a position where other
	 * has Bound 
	 */
	public boolean satisfyBinding(BindingList other) {
		for (int i = 0; i < size(); i++) {
			if (!(get(i).satisfyBinding(other.get(i)))) {
				return false;
			}
		}
		return true;
	}

	public boolean usePartialKeyExtraction() {
		for (int i = 0; i < size(); i++) {
			if (get(i).usePartialKeyExtraction())
				return true;
		}
		return false;
	}

	public int getPartialKeySize() {
		if (partialKeySize<0)
			computePartialKeySize();
		return partialKeySize;
	}

	private void computePartialKeySize() {
		partialKeySize = 0;
		for (int i = 0; i < size(); i++) {
			BindingMode b = get(i);
			if (b.isBound() || b.usePartialKeyExtraction())
				partialKeySize++;
		}
	}

	/**
	 * Creates a BindingList from a string like "BFB" (B: bound, F: free).
	 */
	public static BindingList convertFromString(String string) {
		BindingList paramModes = Factory.makeBindingList();
		
		for (int i = 0; i < string.length(); i++) {
			char currChar = string.charAt(i);
			if (currChar == 'b' || currChar == 'B') {
				paramModes.add(Factory.makeBound());
			} else if (currChar == 'f' || currChar == 'F'){
				paramModes.add(Factory.makeFree());
			}
			else {
				throw new Error("unknown binding mode " + currChar);
			}
		}
		
		return paramModes;
	}
}

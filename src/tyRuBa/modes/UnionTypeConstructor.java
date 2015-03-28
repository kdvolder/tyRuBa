package tyRuBa.modes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

public class UnionTypeConstructor extends TypeConstructor {
	
	private Set<TypeConstructor> elements= new HashSet<TypeConstructor>();

	public UnionTypeConstructor(TypeConstructor tc,
			TypeConstructor otc) {
		Assert.assertFalse(tc.isSuperTypeOf(otc));
		Assert.assertFalse(otc.isSuperTypeOf(tc));
		Assert.assertFalse(tc instanceof UnionTypeConstructor);
		Assert.assertTrue("Only 0 arity type constructors can be unioned together", 
					tc.getTypeArity() == 0 && otc.getTypeArity()==0);
		elements.add(tc);
		if (otc instanceof UnionTypeConstructor) {
			UnionTypeConstructor uotc = (UnionTypeConstructor) otc;
			for (TypeConstructor e : uotc.elements) {
				if (!this.isSuperTypeOf(e)) 
					// Don't add e if already implied by some element
					// in elements.
					elements.add(e);
			}
		}
		else {
			elements.add(otc);
		}
	}

	@Override
	public ConstructorType getConstructorType() {
		throw new Error("Union types are abstract, you can not construct an instance of them directly");
	}

	@Override
	public String getName() {
		boolean first = true;
		StringBuilder str = new StringBuilder();
		for (TypeConstructor e : elements) {
			if (!first) str.append("|");
			str.append(e.getName());
			first = false;
		}
		return str.toString();
	}

	@Override
	public String getParameterName(int i) {
        throw new Error("This is not a user defined type");
    }

	@Override
	public List<TypeConstructor> getSuperTypeConstructor() {
		ArrayList<TypeConstructor> result = new ArrayList<TypeConstructor>();
		result.addAll(elements);
		return result;
	}

	@Override
	public int getTypeArity() {
		return 0;
	}

	@Override
	public boolean isInitialized() {
        throw new Error("This is not a user defined type");
	}

	@Override
	public Class javaEquivalent() {
		throw new Error("Not implemented for union types");
	}

	@Override
	public String toString() {
		return getName();
	}
}

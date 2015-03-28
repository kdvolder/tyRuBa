package tyRuBa.modes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JavaTypeConstructor extends TypeConstructor implements Serializable {

	private final Class javaClass;
	private final String aliasName;

	/** Constructor 
	 * @param aliasName */
	public JavaTypeConstructor(String aliasName, Class javaClass) {
		String name = javaClass.getName();
		if (name.startsWith("java.lang.")) {
			aliasName = name.substring("java.lang.".length());
		} 

		this.aliasName = aliasName;
		
		if (//javaclass.isInterface() || 
			javaClass.isPrimitive()) {
			throw new Error("no primitives types are allowed");
		} else {
			this.javaClass = javaClass;
		}
	}
	
	public JavaTypeConstructor(Class javaClass) {
		this(javaClass.getName(), javaClass);
	}

	public String getName() {
		return aliasName;
	}

	public boolean equals(Object other) {
		if (other != null && this.getClass().equals(other.getClass())) {
			return this.javaClass.equals(((JavaTypeConstructor)other).javaClass);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return javaClass.hashCode();
	}

	public void addSuperType(TypeConstructor superType) throws TypeModeError {
		throw new TypeModeError("Can not add super type for java types " + this);
	}

    public List<TypeConstructor> getSuperTypeConstructor() {
    	ArrayList<TypeConstructor> result = new ArrayList<TypeConstructor>(1);
        if (javaClass.getSuperclass() != null)
            result.add(new JavaTypeConstructor(javaClass.getSuperclass()));
        else if (javaClass.isInterface())
        	result.add(JavaTypeConstructor.theAny);
        Class<?>[] itfs = javaClass.getInterfaces();
        for (int i = 0; i < itfs.length; i++) {
			result.add(new JavaTypeConstructor(itfs[i]));
		}
        return result;
    }

    public int getTypeArity() {
        return 0;
    }

    public String getParameterName(int i) {
        throw new Error("This is not a user defined type");
    }

    /* (non-Javadoc)
     * @see tyRuBa.modes.TypeConstructor#isInitialized()
     */
    public boolean isInitialized() {
        throw new Error("This is not a user defined type");
    }

    public ConstructorType getConstructorType() {
        return ConstructorType.makeJava(javaClass);
    }
    
    public boolean isJavaTypeConstructor() {
        return true;
    }

    public String toString() {
    		return "JavaTypeConstructor("+javaClass+")";
    }
    
	public Class javaEquivalent() {
		return javaClass;
	}
	
}

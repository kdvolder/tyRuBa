/*
 * Created on Aug 13, 2004
 */
package tyRuBa.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.PatternSyntaxException;

import tyRuBa.engine.visitor.TermVisitor;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.ConstructorType;
import tyRuBa.modes.Factory;
import tyRuBa.modes.JavaConstructorType;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeEnv;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.RegularExpression;
import tyRuBa.util.TwoLevelKey;

public class RBJavaObjectCompoundTerm extends RBCompoundTerm {

	public static RBTerm javaClass(String name) {
		try {
			return new RBJavaObjectCompoundTerm(Class.forName(name));
		} catch (ClassNotFoundException e) {
			throw new Error("Class not found:" + name);
		}
	}

	/** A RegularExpression that can be treated as a constant term */
	public static RBTerm regexp(final String re) throws ParseException {
		//		try {
		//			return new RBJavaObjectCompoundTerm(new RE(re) {
		//				private static final long serialVersionUID = 1L;
		//				public String toString() {
		//					return "/"+re+"/";
		//				}
		//			});
		//		} catch (RESyntaxException e) {
		//			throw new ParseException("Syntax error in regexp /"+re+"/"+e.getMessage());
		//		}

		try {
			return new RBJavaObjectCompoundTerm(new RegularExpression(re));
		} catch (PatternSyntaxException e) {
			throw new ParseException("Syntax error in regexp /" + re + "/" + e.getMessage());
		}
	}

	@Override
	public String quotedToString() {
		Object obj = getObject();
		if (obj instanceof String) {
			return (String) obj;
		}
		return super.quotedToString();
	}

	public static final RBTerm theEmptyList = new RBJavaObjectCompoundTerm("[]") {
		public String quotedToString() {
			return "";
		}

		public Type getType(TypeEnv env) {
			return Factory.makeEmptyListType();
		}

		/** 
		 * This "funky" implementation of equals is because even tough we are
		 * a singleton class, we don't know for sure if some deserialization creates
		 * extra copies of me who should be treated as equal.
		 */
		public boolean equals(Object obj) {
			return obj != null && obj.getClass() == this.getClass();
		}

		public int hashCode() {
			return getClass().hashCode();
		}

		public Object up() {
			return new Object[0];
		}

		@Override
		public void unparse(PrintWriter out) {
			out.print("[]");
		}

	};

	private Object arg;

	public RBJavaObjectCompoundTerm(Object arg) {
		//		if (arg instanceof String) {
		//			this.arg = ((String)arg).intern();
		//		} else {
		this.arg = arg;
		//		}
	}

	public ConstructorType getConstructorType() {
		return ConstructorType.makeJava(arg.getClass());
	}

	public RBTerm getArg() {
		return this;
	}

	public RBTerm getArg(int i) {
		if (i == 0) {
			return this;
		} else {
			throw new Error("RBJavaObjectCompoundTerms only have one argument");
		}
	}

	public int getNumArgs() {
		return 1;
	}

	boolean freefor(RBVariable v) {
		return true;
	}

	public boolean isGround() {
		return true;
	}

	protected boolean sameForm(RBTerm other, Frame lr, Frame rl) {
		return equals(other);
	}

	public Type getType(TypeEnv env) throws TypeModeError {
		return ((JavaConstructorType) getConstructorType()).getType();
	}

	public int formHashCode() {
		return 9595 + arg.hashCode();
	}

	public int hashCode() {
		return 9595 + arg.hashCode();
	}

	public BindingMode getBindingMode(ModeCheckContext context) {
		return Factory.makeBound();
	}

	public void makeAllBound(ModeCheckContext context) {
	}

	public boolean equals(Object x) {
		if (x.getClass().equals(this.getClass())) {
			return arg.equals(((RBJavaObjectCompoundTerm) x).arg);
		} else {
			return false;
		}
	}

	public Object accept(TermVisitor v) {
		return v.visit(this);
	}

	public boolean isOfType(TypeConstructor t) {
		return t.isSuperTypeOf(getTypeConstructor());
	}

	public Object up() {
		return arg;
	}

	public Frame unify(RBTerm other, Frame f) {
		if (other instanceof RBVariable)
			return other.unify(this, f);
		else if (equals(other))
			return f;
		else
			return null;
	}

	/**
	 * @see tyRuBa.util.TwoLevelKey#getFirst()
	 */
	public String getFirst() {
		if (arg instanceof String) {
			String str = (String) arg;
			int firstindexofhash = str.indexOf('#');
			if (firstindexofhash == -1) {
				return " ";
			} else {
				return str.substring(0, firstindexofhash);//.intern();
			}
		} else if (arg instanceof Number) {
			return ((Number) arg).toString();
		} else if (arg instanceof TwoLevelKey) {
			return ((TwoLevelKey) arg).getFirst();
		} else {
			return "DummyFirstLevelKey";
		}
	}

	/**
	 * @see tyRuBa.util.TwoLevelKey#getSecond()
	 */
	public Object getSecond() {
		if (arg instanceof String) {
			String str = (String) arg;
			int firstindexofhash = str.indexOf('#');
			if (firstindexofhash == -1) {
				return str;
			} else {
				return str.substring(firstindexofhash);//.intern();
			}
		} else if (arg instanceof Number) {
			return ((Number) arg).toString();
		} else if (arg instanceof TwoLevelKey) {
			return ((TwoLevelKey) arg).getSecond();
		} else {
			return this;
		}
	}

	//	public String toString() {
	//		return arg.toString();
	//	}

	@Override
	public void unparse(PrintWriter out) {
		if (arg instanceof String) {
			String string = (String) arg;
			out.append('"');
			for (int i = 0; i < string.length(); i++) {
				char c = string.charAt(i);
				switch (c) {
				case '\\':
					out.append("\\\\");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '"':
					out.append("\\\"");
					break;
				default:
					out.append(c);
					break;
				}
			}
			out.append('"');
		} else
			out.append(arg.toString());
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		//		if (arg instanceof String) {
		//			arg = ((String)arg).intern();
		//		}
	}

	public int intValue() {
		if (arg instanceof Integer) {
			return ((Integer) arg).intValue();
		} else {
			return super.intValue();
		}
	}

	public Object getObject() {
		return this.arg;
	}

}

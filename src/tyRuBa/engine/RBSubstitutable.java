package tyRuBa.engine;

import java.io.PrintWriter;

/**
 * @author kdvolder
 */
public abstract class RBSubstitutable extends RBTerm {
	
	protected String name;
	
	RBSubstitutable(String name) {
		this.name = name;
	}

	public String name() {
		return name;
	}

	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public void unparse(PrintWriter out) {
		out.print(name);
	}

	public boolean equals(Object obj) {
		return (obj instanceof RBSubstitutable)
			&& ((RBSubstitutable) obj).name == this.name;
	}	

}

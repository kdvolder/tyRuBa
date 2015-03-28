package tyRuBa.engine;

import java.io.Serializable;

import annotations.Feature;
import annotations.Export;

/**
 * @category JDBC
 */
@Feature(names="Validator")
public class Validator implements Serializable, IValidator {

	public static Validator theNeverValid = new Validator(false);

	@Export(to="NONE")
	public static Validator theAlwaysValid = new Validator() {
	    public void invalidate() {
	    	throw new Error("Not allowed!");
	    }
	};

    private boolean isOutdated = true;
	private boolean isValid = true;
    private long handle = -1;
    	
    public Validator() {}

    private Validator(boolean isValid) {this.isValid=isValid;}

    public long handle() {
		return handle;
	}
	
	public void setHandle(long handle) {
		this.handle = handle;
	}

	public boolean isValid() {
		return isValid;
	}
    
    public void invalidate() {
        isValid = false;
    }

	public String toString() {
		return "Validator("+handle+","
		  + (isOutdated ? "OUTDATED" : "UPTODATE") +"," 
		  + (isValid ? "VALID" : "INALIDATED") + ")";
	}

	public boolean isOutdated() {
		return isOutdated;
	}

	public void setOutdated(boolean flag) {
		isOutdated = flag;
	}
	
}
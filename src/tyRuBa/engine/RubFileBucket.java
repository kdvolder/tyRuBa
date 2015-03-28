package tyRuBa.engine;

import java.io.IOException;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

/**
 * A RuleBaseBucket that reads input from a .rub file when asked to
 * update its facts.
 * 
 * The /rub file is assumed to be a text file containing facts in TyRuBa
 * syntax. 
 * 
 * @author kdvolder
 */
public class RubFileBucket extends RuleBaseBucket {
	
	private static int bucketLoads = 0;
	
	public int getBucketLoadCount() {
		return bucketLoads;
	}
	
	private String myfile;
	
	public RubFileBucket(FrontEnd fe,String filename) {
		super(fe,filename);
		myfile = filename;
	}

	public void update() throws ParseException, TypeModeError {
		try {
			load(myfile);
			bucketLoads++;
		}
		catch (IOException e) {
			throw new Error("IOError for file "+myfile+": "+e.getMessage());
		}
	}
	
	public String toString() {
		return "RubFileBucket("+myfile+")";
	}

}

package tyRuBa.engine;
	
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class SimpleRuleBaseBucket extends RuleBaseBucket {
	
	StringBuffer mystuff = null; // some stuff I should parse

	public SimpleRuleBaseBucket(FrontEnd frontEnd) {
		super(frontEnd, null);
	}
	
	public SimpleRuleBaseBucket(FrontEnd frontEnd, String id) {
		super(frontEnd, id);
	}
	
	public SimpleRuleBaseBucket(FrontEnd frontend, String id, SimpleRuleBaseBucket copyInitialContents) {
		super(frontend,id);
		if (copyInitialContents!=null && copyInitialContents.mystuff!=null) {
			mystuff = new StringBuffer(copyInitialContents.mystuff.toString());
		}
	}

	public synchronized void addStuff(String toParse) {
		if (mystuff == null) 
			mystuff = new StringBuffer();
		mystuff.append(toParse + "\n");
		setOutdated();
	}
	
	public synchronized void clearStuff() {
		mystuff = null;
		setOutdated();
	}

	public synchronized void update() throws ParseException, TypeModeError {
		if (mystuff != null) {
			parse(mystuff.toString());
//			mystuff = null;
		}
	}

}

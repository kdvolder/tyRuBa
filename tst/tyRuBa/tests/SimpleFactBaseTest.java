package tyRuBa.tests;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.TyRuBaConf;


public class SimpleFactBaseTest extends TyrubaTest {
	
	public SimpleFactBaseTest(String arg0) {
		super(arg0);
	}
	
	protected void setUp() throws Exception {
		super.setUpNoFrontend();
		TyRuBaConf conf = new TyRuBaConf();
		frontend = new FrontEnd(conf); 
	}
		
	public void testAddAndQuery() throws Exception {
		frontend.parse("test :: String PERSISTENT MODES (F) IS NONDET END");
		frontend.parse("test(Kris).");
		test_must_succeed("test(Kris)");
		test_must_equal("test(?x)", "?x", "Kris");
		test_resultcount("test(?x)", 1);
	}

	public void testIntFacts() throws Exception {
		frontend.parse("test :: Integer PERSISTENT MODES (F) IS NONDET END");
		frontend.parse("test(78).");
		test_must_succeed("test(78)");
		test_must_equal("test(?x)", "?x", "78");
		test_resultcount("test(?x)", 1);
	}
	
	public void testAddAndQuery2() throws Exception {
		frontend.parse("test :: String, String PERSISTENT MODES (F,F) IS NONDET END");
		frontend.parse("test(Kris,DeVolder).");
		test_must_equal("test(Kris,?x)", "?x", "DeVolder");
		test_must_succeed("test(Kris,DeVolder)");
		test_must_succeed("test(?,?)");
		test_must_equal("test(?x,DeVolder)", "?x", "Kris");
		test_must_equal("test(?x,?)", "?x", "Kris");
		test_must_equal("test(?x,?y)", "?x", "Kris");
		test_resultcount("test(?x,?y)", 1);
	}
	
	public void testConjunctivate() throws Exception {
		int resultSize = 15;
		int facts = 30;
		frontend.parse("test :: String, String PERSISTENT MODES (F,F) IS NONDET END");
		for (int i = 0; i < facts; i++) {
			frontend.parse("test(\"" +i+ "\", \"" +(i+1)+ "\").");
		}
		String query = "";
		for (int i = 0; i < facts-resultSize+1; i++) {
			if (i>0)
				query += ", ";
			query += "test(?x"+i+", ?x"+(i+1)+")"; 
		}
		String[] expected = new String[resultSize];
		for (int i = 0; i < expected.length; i++) {
			expected[i]="\""+i+"\"";
		}
		test_must_findall(query, "?x0", expected);
	}
	
	public void testReconnect() throws Exception {
		int facts = 30;
		frontend.parse("test :: String, String PERSISTENT MODES (F,F) IS NONDET END");
		String[] expected = new String[facts];
		for (int i = 0; i < facts; i++) {
			expected[i] = "\""+i+"\"";
			frontend.parse("test(a, \"" +i+ "\").");
			frontend.parse("test(\""+i+"\", a).");
		}
		test_must_findall("test(a,?x)", "?x", expected);
		test_must_findall("test(?x,a)", "?x", expected);
		frontend.shutdown();
		
		TyRuBaConf conf = new TyRuBaConf();
		conf.setCleanStart(false); // reconnect!
		frontend = new FrontEnd(conf);
		frontend.parse("test :: String, String PERSISTENT MODES (F,F) IS NONDET END");
		test_must_findall("test(a,?x)", "?x", expected);
		test_must_findall("test(?x,a)", "?x", expected);		
	}	

	public void testDuplicateStrings() throws Exception {
		
		frontend.parse("strings :: String, String " +
				"PERSISTENT MODES " +
				"	(F,F) IS NONDET " +
				"	(B,B) IS SEMIDET " +
				"	(B,F) IS NONDET " +
				"	(F,B) IS NONDET " +
				"END");
		
		frontend.parse("strings(Fawlty,Towers).");
		frontend.parse("strings(Ivory,Towers).");
		
		test_must_findall("strings(?x,Towers)", "?x", new String[] {"Fawlty","Ivory"});
		test_must_findall("strings(?x,?y)", "?x", new String[] {"Fawlty","Ivory"});
		test_must_equal("strings(Ivory,?x)","?x","Towers");
		test_must_succeed("strings(Fawlty,Towers)");
		test_must_succeed("strings(Ivory,Towers)");
	}
	
	public void testDuplicateAsStrings() throws Exception {
		frontend.parse("TYPE AsString AS String");
		frontend.parse("strings :: AsString, AsString " +
				"PERSISTENT MODES " +
				"	(F,F) IS NONDET " +
				"	(B,B) IS SEMIDET " +
				"	(B,F) IS NONDET " +
				"	(F,B) IS NONDET " +
				"END");
		frontend.parse("strings(Fawlty::AsString,Towers::AsString).");
		frontend.parse("strings(Ivory::AsString,Towers::AsString).");
		
		test_must_findall("strings(?x,Towers::AsString)", "?x", new String[] {"Fawlty::AsString","Ivory::AsString"});
		test_must_findall("strings(?x,?y)", "?x", new String[] {"Fawlty::AsString","Ivory::AsString"});
		test_must_equal("strings(Ivory::AsString,?x)","?x","Towers::AsString");
		test_must_succeed("strings(Fawlty::AsString,Towers::AsString)");
		test_must_succeed("strings(Ivory::AsString,Towers::AsString)");
		
	}

}

package tyRuBa.tests;

import tyRuBa.jobs.ProgressMonitor;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.engine.RuleBase;
import tyRuBa.engine.SimpleRuleBaseBucket;

public class ParserTest extends TyrubaTest {

	public ParserTest(String arg0) {
		super(arg0);
	}

	public void testParseExpression() throws ParseException, TypeModeError {
		try {
			test_must_fail("string_append(?x,?y,abc) string_append(?y,?z,abc)");
			fail("Should throw a parse exception");
		}
		catch (ParseException e) {
		}
	}
	
	public void testEndOfFileComment() throws Exception {
		frontend.parse("test :: String \n" +
				"MODES (F) IS NONDET END\n" +
				"test(Kris).\n" +
				"//Should be ok");
	}

}

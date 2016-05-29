/*
 * Created on Nov 20, 2008
 */
package tyRuBa.tests;

import java.util.ArrayList;
import java.util.List;

import tyRuBa.engine.RuleBase;
import tyRuBa.modes.Factory;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.Insert;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.TyrubaException;

public class TypeAliasTest extends TyrubaTest {

	public TypeAliasTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		TyrubaTest.initfile = true;
		RuleBase.useCache = true;
		RuleBase.silent = true;
		super.setUp();
	}

	public void testAlias() throws TypeModeError, ParseException, TyrubaException {
		frontend.defineTypeAlias("Thing", TypeAliasTest.class);
		frontend.parse("test :: Thing\n" + 
				"MODES \n" + 
				" (F) IS SEMIDET\n" + 
				"END\n");
		frontend.parse("same :: Thing, Thing\n" + 
				"MODES \n" + 
				" (B,F) IS SEMIDET\n" + 
				" (F,B) IS SEMIDET\n" + 
				"END\n" +
				"same(?x,?x) :- Thing(?x).");
				
		frontend.parse("sameObj :: Object, Object\n" + 
				"MODES \n" + 
				" (B,F) IS SEMIDET\n" + 
				" (F,B) IS SEMIDET\n" + 
				"END\n" +
				"sameObj(?x,?x).");
		
		Connection conn = new Connection(frontend);
		PreparedInsert insert = conn.prepareInsert("test(!x)");
		insert.put("!x", this);
		insert.executeInsert();
		
		test_must_equal("test(?x)", "?x", this);
		test_must_equal("test(?x), same(?x,?y)", "?y", this);
		test_must_equal("test(?x), sameObj(?x,?y)", "?y", this);
		
	}

	public void testAliasItf() throws TypeModeError, ParseException, TyrubaException {
		frontend.defineTypeAlias("Thing", List.class);
		frontend.parse("test :: Thing\n" + 
				"MODES \n" + 
				" (F) IS SEMIDET\n" + 
				"END\n");
		frontend.parse("same :: Thing, Thing\n" + 
				"MODES \n" + 
				" (B,F) IS SEMIDET\n" + 
				" (F,B) IS SEMIDET\n" + 
				"END\n" +
				"same(?x,?x) :- Thing(?x).");
				
		frontend.parse("sameObj :: Object, Object\n" + 
				"MODES \n" + 
				" (B,F) IS SEMIDET\n" + 
				" (F,B) IS SEMIDET\n" + 
				"END\n" +
				"sameObj(?x,?x).");
		
		Connection conn = new Connection(frontend);
		PreparedInsert insert = conn.prepareInsert("test(!x)");
		insert.put("!x", new ArrayList());
		insert.executeInsert();
		
		test_must_equal("test(?x)", "?x", new ArrayList());
		test_must_equal("test(?x), same(?x,?y)", "?y", new ArrayList());
		test_must_equal("test(?x), sameObj(?x,?y)", "?y", new ArrayList());
		
	}
}

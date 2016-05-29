package tyRuBa.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBJavaObjectCompoundTerm;
import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.factbase.berkeley_db.TyRuBaTupleBinding;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.parser.TyRuBaParser;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.TyrubaException;
import junit.framework.TestCase;

public class UnparseTest extends TyrubaTest {

	public UnparseTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testStringUnparse() throws ParseException, TypeModeError {
		String[] testStrings = {
			"Hola pola",
			"\"A backslash \\ a \n and a quote \"",
			"What about tabs? like this \t",
			"Boundary case \\"
		};
		for (String string : testStrings) {
			RBJavaObjectCompoundTerm term = (RBJavaObjectCompoundTerm) RBJavaObjectCompoundTerm.makeJava(string);
			assertEquals(string, term.getObject()); // The contained object should be exactly as what we put in
			
			ByteArrayOutputStream bytesOut;
			PrintWriter out = new PrintWriter(bytesOut=new ByteArrayOutputStream());
			term.unparse(out);
			out.close();
			String unparsed = bytesOut.toString();
			RBTerm parsed = frontend.makeTermFromString(unparsed);
			assertEquals("Parse/Unparse test \""+string+"\"", term, parsed);
		}
	}
	
	public void testRepAsString() throws ParseException, TypeModeError {
		frontend.parse("TYPE Foo AS String");
		String[] testStrings = {
//			"Hola pola",
			"\"A backslash \\ a \n and a quote \"",
			"What about tabs? like this \t",
			"Boundary case \\"
		};
		TypeConstructor foo = frontend.findTypeConst("Foo");
		for (String string : testStrings) {
			RBRepAsJavaObjectCompoundTerm term = (RBRepAsJavaObjectCompoundTerm) frontend.makeTypeCast(foo, string);
			assertEquals(string, term.getValue()); // The contained object should be exactly as what we put in
			
			ByteArrayOutputStream bytesOut;
			PrintWriter out = new PrintWriter(bytesOut=new ByteArrayOutputStream());
			term.unparse(out);
			out.close();
			String unparsed = bytesOut.toString();
			RBTerm parsed = frontend.makeTermFromString(unparsed);
			assertEquals("Parse/Unparse test \""+string+"\"", term, parsed);
		}
	}

	public void testList() throws ParseException, TypeModeError {
		String[] testStrings = {
				"[]",
				"[\"Kablo wi\", \"foo%crazy\"]",
				"[1, 2, 3]",
				"[1]",
				"[1 | 2]",
				"[1, 2 | 3]"
		};
		for (String string : testStrings) {
			RBTerm term = frontend.makeTermFromString(string);
			
			ByteArrayOutputStream bytesOut;
			PrintWriter out = new PrintWriter(bytesOut=new ByteArrayOutputStream());
			term.unparse(out);
			out.close();			
			String unparsed = bytesOut.toString();
			
			assertEquals("Parse/Unparse test \""+string+"\"", string, unparsed);
		}
	}
	
	public void testTuple() throws Exception {
		String[] testStrings = {
				"<>",
				"<\"Kablo wi\", \"foo%crazy\">",
				"<1, 2, 3>",
				"<1, \"a\", <\"b\", \"c\">>",
				"<<>, <>, <\"a\">>"
		};
		for (String string : testStrings) {
			RBTerm term = frontend.makeTermFromString(string);
			
			ByteArrayOutputStream bytesOut;
			PrintWriter out = new PrintWriter(bytesOut=new ByteArrayOutputStream());
			term.unparse(out);
			out.close();			
			String unparsed = bytesOut.toString();
			
			assertEquals("Parse/Unparse test \""+string+"\"", string, unparsed);
		}
	}

	public void testCompound() throws Exception {
		frontend.parse("TYPE Bork AS <String,Integer,Integer>");
		String[] testStrings = {
				"Bork<\"a\", 1, 2>",
				"Bork<\"@zu3eus!! 11\\n\", 1, 2>"
		};
		for (String string : testStrings) {
			RBTerm term = frontend.makeTermFromString(string);
			
			ByteArrayOutputStream bytesOut;
			PrintWriter out = new PrintWriter(bytesOut=new ByteArrayOutputStream());
			term.unparse(out);
			out.close();			
			String unparsed = bytesOut.toString();
			
			assertEquals("Parse/Unparse test \""+string+"\"", string, unparsed);
		}
	}

	/**
	 * This test fails right now. It's failure is the reason why the JUnit browser
	 * is broken in JQuery.
	 */
	public void testRBQuotedUnparse() throws ParseException, TypeModeError, TyrubaException {
		frontend.parse("TYPE Method AS String");
		frontend.parse("plunk :: Method, String " +
				       "MODES (B,F) IS DET END ");
		frontend.parse("plunk(?it,{?it}).");
		
		String[] testTermStr = {
//			"foo::Method",
			"\"foo%.#$@  , , \"::Method"
		};
		
		for (int i = 0; i < testTermStr.length; i++) {
			RBTerm term = frontend.makeTermFromString(testTermStr[i]);
			Object obj = term.up();
			String plunked = frontend.getStringProperty(obj, "plunk"); 
			assertEquals(term, frontend.makeTermFromString(plunked));
		}
	}
	
	public void testRBQuotedAppend() throws Exception {
		frontend.parse("q_append :: Object, Object, String " +
				       "MODES (B,B,F) IS DET END");
		frontend.parse("q_append(?x,?y,{?x?y}).");
		Connection conn = new Connection(frontend);
		PreparedQuery q = conn.prepareQuery("q_append(!x,!y,?r)");
		q.put("!x", "Hello");
		q.put("!y", " World!");
		ResultSet results = q.executeQuery();
		assertTrue(results.next());
		assertEquals("Hello World!", results.getString("?r"));
		assertFalse(results.next());
	}
	
	public void testRBQuotedList() throws Exception {
		frontend.parse("q_append :: Object, Object, String " +
				       "MODES (B,B,F) IS DET END");
		frontend.parse("q_append(?x,?y,{?z}) :- " +
				"equals(?z,[?x,?y]).");
		Connection conn = new Connection(frontend);
		PreparedQuery q = conn.prepareQuery("q_append(!x,!y,?r)");
		q.put("!x", "Hello");
		q.put("!y", " World!");
		ResultSet results = q.executeQuery();
		assertTrue(results.next());
		assertEquals("Hello World!", results.getString("?r"));
		assertFalse(results.next());
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}

/*
 * Created on Feb 16, 2004
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package tyRuBa.tests;

import java.lang.reflect.Array;
import java.util.Arrays;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.TyRuBaConf;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.Insert;
import tyRuBa.tdbc.PreparedInsert;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.ResultSet;
import tyRuBa.tdbc.Query;
import tyRuBa.tdbc.TyrubaException;
import junit.framework.TestCase;

/**
 * @author kdvolder
 */
public class TDBCTest extends TestCase {

	Connection conn;
	private FrontEnd fe;
	
	protected void setUp() throws Exception {
		super.setUp();
		
		fe = new FrontEnd(new TyRuBaConf());

		fe.parse("TYPE Method  AS String");
		fe.parse("TYPE Field   AS String");
		fe.parse("TYPE Member = Method | Field");
		fe.parse("foo :: Method,String \n" +
				"MODES (F,F) IS NONDET END");
		fe.parse("foo(booh::Method,booh).");

		fe.parse("fooMem :: Member,String \n" +
				 "MODES (F,F) IS NONDET END");
		fe.parse("fooMem(f_booh::Field,f_booh).");
		fe.parse("fooMem(m_booh::Method,m_booh).");
		
		
		fe.parse("method :: Method " +
				"MODES (F) IS NONDET END");
		fe.parse("field :: Field " +
				"MODES (F) IS NONDET END");
		fe.parse("call :: Method, Method " +
				"MODES (F,F) IS NONDET END");
		
		fe.parse("method(Dana::Method).");
		fe.parse("method(Kris::Method).");
		fe.parse("field(CS::Field).");
		fe.parse("call(Dana::Method, Kris::Method).");

		conn = new Connection(fe);
	}


	protected void tearDown() throws Exception {
		fe.shutdown();
		super.tearDown();
	}
	
	public void testNoTypeCheckList() throws Exception {
		fe.parse("listOfStuff :: String, [String] " +
		"MODES (F,F) IS NONDET END");
		fe.parse("listOfStuff(Cool,[Check,this,out]).");

		PreparedQuery stat = conn.prepareNoTypeCheckQuery("listOfStuff(?x,!l)");
		String[] l = new String[] {"Check","this","out"};
		stat.put("!l",l);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String n = results.getString("?x");
			assertEquals("Cool",n);
		}
		assertEquals(1,count);
	}

    public void testNoTypeCheck() {
    	try {
			PreparedQuery q = conn.prepareNoTypeCheckQuery("fooZorba(?x)");
	    	fail("Should have undefined predicate error");
		} catch (TyrubaException e) {
			assertTrue(e.getMessage().contains("fooZorba"));
		}
    }
    
    public void testAnnoyingDNFConversion() throws Exception {
		PreparedQuery q = conn.prepareNoTypeCheckQuery("(method(?x);field(?x)),call(?x,?)");
		ResultSet r = q.executeQuery();
		assertTrue(r.next());
		assertEquals(conn.parseTerm("Dana::Method"), r.getObject("?x"));
		assertFalse(r.next());		
    }
	
	public void testQuery() throws Exception {
		Query stat = conn.createQuery();
		ResultSet results = stat.executeQuery("string_append(?x,?y,abcde)");
		int count = 0;
		while (results.next()) {
			count++;
			String x = results.getString("?x");
			String y = results.getString("?y");
			assertEquals(x+y,"abcde");
		}
		assertEquals(count,6);
	}

	public void testNoColsQuery() throws Exception {
		Query stat = conn.createQuery();
		ResultSet results = stat.executeQuery("string_append(ab,cde,abcde)");
		assertTrue(results.next());
		assertFalse(results.next());

		results = stat.executeQuery("string_append(ab,cd,abcde)");
		assertFalse(results.next());
	}
	
	public void testPreparedQuery() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		String x = "a b c";
		String y = " d e";
		stat.put("!x",x);
		stat.put("!y",y);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String xy = results.getString("?xy");
			assertEquals(x+y,xy);
		}
		assertEquals(count,1);
	}

	public void testPreparedQueryMissingVar() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		String x = "a b c";
		String y = " d e";
		stat.put("!x",x);
		try {
			ResultSet results = stat.executeQuery();
			fail("Should have detected the problem that !y has not been put.");
		}
		catch (TyrubaException e) {
			// ok 
		}
	}
	
	public void testGetOutputVariables() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		String[] vars = stat.getOutputVariables();
		assertEquals(1, vars.length);
		assertEquals("?xy", vars[0]);
	}

	public void testGetOutputVariables2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(?x,?z,!xy) ; string_append(?a,?x,!xy)");
		String[] vars = stat.getOutputVariables();
		assertEquals(1, vars.length);
		assertEquals("?x", vars[0]);
	}
	
	public void testGetOutputVariables3() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(?x,?y,!z)");
		String[] vars = stat.getOutputVariables();
		assertEquals(2, vars.length);
		Arrays.sort(vars);
		assertEquals("?x", vars[0]);
		assertEquals("?y", vars[1]);
	}
	
	public void testPreparedQueryBadType() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		try {
			stat.put("!x",123);
			fail("This should have thrown an exception. !m MUST be a string");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage()); 
		}
	}

	public void testPreparedQueryBadVar() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("string_append(!x,!y,?xy)");
		try {
			stat.put("!m","abc");
			fail("This should have thrown an exception. !m is not defined");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage()); 
		}
	}

	public void testPreparedQueryBadType2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(!m::Method,?n)");
		try {
			stat.put("!m",123);
			fail("This should have thrown an exception. !m MUST be a string");
		} catch (TyrubaException e) {
			// good! 
		}
	}
	
	public void testPreparedQueryUDTypeOut() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(?m::Method,!n)");
		String n = "booh";
		stat.put("!n",n);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String m = results.getString("?m");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}

	public void testPreparedQueryUDTypeOut2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("fooMem(?m::Method,!n)");
		String n = "m_booh";
		stat.put("!n",n);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String m = results.getString("?m");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}
	
	public void testPreparedQueryListTypeIn() throws TyrubaException, ParseException, TypeModeError {
		fe.parse("listOfStuff :: String, [String] " +
				"MODES (F,F) IS NONDET END");
		fe.parse("listOfStuff(Cool,[Check,this,out]).");
		
		PreparedQuery stat = conn.prepareQuery("listOfStuff(?x,!l)");
		String[] l = new String[] {"Check","this","out"};
		stat.put("!l",l);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String n = results.getString("?x");
			assertEquals("Cool",n);
		}
		assertEquals(1,count);
	}
	
	public void testPreparedQueryObjectUDTypeIn() throws TyrubaException, ParseException, TypeModeError {
		fe.parse("stuff :: Object, String " +
				"MODES (F,F) IS NONDET END");
		fe.parse("stuff(foo::Method,Hola).");

		PreparedQuery stat = conn.prepareQuery("equals(?x,foo::Method)"); 
		  // A hack to get an foo::Method object
		ResultSet results = stat.executeQuery();
		results.next();
		Object fooMethod = results.getObject("?x");

		//Now see if we can use this fooMethod object in another query as a parameter.
		stat = conn.prepareQuery("stuff(!x,?v)");
		stat.put("!x",fooMethod);
		results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String n = results.getString("?v");
			assertEquals("Hola",n);
		}
		assertEquals(1,count);
	}
	
	public void testPreparedQueryUDTypeIn() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("foo(!m::Method,?n)");
		String m = "booh";
		stat.put("!m",m);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			String n = results.getString("?n");
			assertEquals(m,n);
		}
		assertEquals(count,1);
	}
	
	public void testPreparedQueryUDTypeIn2() throws TyrubaException {
		PreparedQuery stat = conn.prepareQuery("call(!m,?n)");
		Object m = conn.parseTerm("Dana::Method");
		Object nExpect = conn.parseTerm("Kris::Method");
		stat.put("!m",m);
		ResultSet results = stat.executeQuery();
		int count = 0;
		while (results.next()) {
			count++;
			Object n = results.getObject("?n");
			assertEquals(nExpect,n);
		}
		assertEquals(count,1);
	}
	
	public void testPreparedQueryUDTypeIn3() throws TyrubaException, TypeModeError {
		Type memberType = conn.findType("Member");
		PreparedQuery stat = conn.prepareQuery("call(!m,?n)");
		Object m = conn.parseTerm("Dana::Field");
		stat.put("!m", m, memberType);
		ResultSet results = stat.executeQuery();
		assertFalse(results.next());
	}
	
	public void testPreparedQueryUDTypeIn4() throws TyrubaException {
		Type methodType = conn.findType("Method");
		PreparedQuery stat = conn.prepareQuery("call(!m,?n)");
		Object m = conn.parseTerm("Dana::Field");
		try {
			stat.put("!m", m, methodType);
			fail("Should have gotten a type Error");
			ResultSet results = stat.executeQuery();
			assertFalse(results.next());
		} catch (TyrubaException e) {
			assertTrue(e.getMessage().contains("Type"));
		}
	}
	
	public void testPreparedQueryUDTypeIn5() throws TyrubaException {
		Type assumedType = conn.findType("String");
		PreparedQuery stat = conn.prepareQuery("call(!m,?n)");
		Object m = conn.parseTerm("Dana");
		try {
			stat.put("!m", m, assumedType);
			fail("Should have gotten a type Error");
			ResultSet results = stat.executeQuery();
			assertFalse(results.next());
		} catch (TyrubaException e) {
			assertTrue(e.getMessage().contains("Type"));
		}
	}
	
	public void testPreparedQueryUDTypeIn6() throws TyrubaException, TypeModeError {
		Type assumedType = conn.findType("Object");
		PreparedQuery stat = conn.prepareQuery("call(!m,?n)");
		Object m = conn.parseTerm("Dana");
		stat.put("!m", m, assumedType);
		ResultSet results = stat.executeQuery();
		assertFalse(results.next());
	}
	
	public void testInsert() throws Exception {
		Insert ins = conn.createInsert();
		ins.executeInsert("foo(bih::Method,bah).");
		
		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(bih::Method,?bah)");

		int count = 0;
		while (results.next()) {
			count++;
			String bah = results.getString("?bah");
			assertEquals(bah,"bah");
		}
		assertEquals(count,1);
	}

	public void testPreparedInsert() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh)");

		ins.put("!duh","bim");
		ins.executeInsert();
		
		ins.put("!duh","bam");
		ins.executeInsert();

		ins.put("!duh","bom");
		ins.executeInsert();
		
		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(clock::Method,?sound)");

		int count = 0;
		while (results.next()) {
			count++;
			String sound = results.getString("?sound");
			assertTrue(sound.length()==3 && sound.startsWith("b") && sound.endsWith("m"));
		}
		assertEquals(count,3);
	}

	public void testPreparedInsertMissingVar() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(!dah::Method,!duh).");

		ins.put("!duh","abc");
		try {
			ins.executeInsert();
			fail("Should have made an error: the variable !dah has not been put");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertBadType() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh).");

		try {
			ins.put("!duh",1);
			fail("Should have made an error: The variable !duh should be a string.");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertBadVar() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(clock::Method,!duh).");

		try {
			ins.put("!dah","abc");
			fail("Should have made an error: the variable !dah is unknown");
		} catch (TyrubaException e) {
			System.err.println(e.getMessage());
		}
	}

	public void testPreparedInsertUDType() throws Exception {
		PreparedInsert ins = conn.prepareInsert("foo(!dah::Method,!duh).");

		ins.put("!dah","abc");
		ins.put("!duh","abc");
		
		ins.executeInsert();

		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(?out::Method,abc)");

		int count = 0;
		while (results.next()) {
			count++;
			String out = results.getString("?out");
			assertEquals(out,"abc");
		}
		assertEquals(count,1);
	}
	
	public void testUpTermsEquals() throws ParseException, TypeModeError, TyrubaException {
		fe.parse("foo(zzzz::Method,a).");
		fe.parse("foo(zzzz::Method,b).");
		Query q = conn.createQuery();
		ResultSet results = q.executeQuery("foo(?x,a);foo(?x,b)");
		results.next(); // 1st result
		Object obj1 = results.getObject("?x");
		results.next(); // 2nd result
		Object obj2 = results.getObject("?x");
		assertTrue(obj1.equals(obj2));
		assertFalse(results.next());
	}
	
	public void testEqualsBug() throws Exception {
		PreparedQuery q = conn.prepareQuery("equals(!this,?e)");
		Object obj = conn.parseTerm("foo::Method");
		q.put("!this", obj);
		ResultSet results = q.executeQuery();
		assertTrue(results.next());
		assertEquals(obj, results.getObject("?e"));
	}

	public void testEqualsBug2() throws Exception {
		PreparedQuery q = conn.prepareQuery("equals(!this,?e::Method)");
		Object obj = conn.parseTerm("foo::Method");
		q.put("!this", obj);
		ResultSet results = q.executeQuery();
		assertTrue(results.next());
		assertEquals("foo", results.getObject("?e"));
	}
	
}

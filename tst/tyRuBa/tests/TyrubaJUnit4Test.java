package tyRuBa.tests;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RuleBase;
import tyRuBa.engine.TyRuBaConf;
import tyRuBa.jobs.ProgressMonitor;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.ElementSource;

import static org.junit.Assert.*;

public class TyrubaJUnit4Test {

	FrontEnd frontend;
	static public boolean initfile = true;
	
	protected void setUp() throws Exception {
		setUp(false); // default setup without reconnecting
	}

	protected void setUp(boolean reconnect) throws Exception {
//		frontend = new FrontEnd(initfile,true);
		TyRuBaConf conf = new TyRuBaConf();
//		conf.setPersistenceConf(new BerkeleyDBConf());
//		conf.setPersistenceConf(new FileBasedPersistenceConf());
		conf.setCleanStart(!reconnect);
		frontend = new FrontEnd(conf);
//		public FrontEnd(boolean loadInitFile, File storagePath, String dbURL, boolean persistent, ProgressMonitor mon,boolean clean,boolean enableBackgroundCleaning) {
		//		RuleBase.silent = true;
	}

	@After
	public void tearDown() throws Exception {
		if (frontend!=null) {
			frontend.shutdown();
			frontend = null;
		}
	}

	protected void setUpNoFrontend() throws Exception {
		RuleBase.silent = true;
	}

	protected void setUp(ProgressMonitor mon) throws Exception {
		TyRuBaConf conf = new TyRuBaConf();
		conf.setLoadInitFile(initfile);
		conf.setProgressMonitor(mon);
		frontend = new FrontEnd(conf);
	}

	void test_must_succeed(String query) throws ParseException, TypeModeError {
		ElementSource result = frontend.frameQuery(query);
		assertTrue(result.hasMoreElements());
	}
	
	void test_must_succeed(String query, QueryEngine qe) throws ParseException,
	TypeModeError {
		ElementSource result = qe.frameQuery(query);
		assertTrue(result.hasMoreElements());
	}
	
	void test_must_equal(String query, String var, String expected) throws ParseException, TypeModeError {
		ElementSource result = frontend.varQuery(query, var);
		assertEquals(frontend.makeTermFromString(expected), result.nextElement());
		assertFalse("More elements than expected", result.hasMoreElements());
	}

	void test_must_equal(String query, String var, Object expected) throws ParseException, TypeModeError {
		ElementSource result = frontend.varQuery(query, var);
		assertEquals(expected, ((RBTerm)result.nextElement()).up());
		assertFalse(result.hasMoreElements());
	}
	
	void test_must_equal(String query, String[] vars, String[] expected) throws ParseException, TypeModeError {
		for (int i = 0; i < vars.length; i++) {
			ElementSource result = frontend.varQuery(query, vars[i]);
			assertEquals(frontend.makeTermFromString(expected[i]), result.nextElement());
			assertFalse(result.hasMoreElements());
		}
	}

	void test_must_fail(String query) throws ParseException, TypeModeError {
		ElementSource result = frontend.frameQuery(query);
		assertFalse(result.hasMoreElements());
	}
	
	void test_must_fail(String query, QueryEngine qe) throws ParseException,
	TypeModeError {
		ElementSource result = qe.frameQuery(query);
		assertFalse(result.hasMoreElements());
	}

	void test_must_findall(String query, String var, String[] expected) throws ParseException, TypeModeError {
		Set expectedSet = new HashSet();
		for (int i = 0; i < expected.length; i++) {
			expectedSet.add(frontend.makeTermFromString(expected[i]));
		}
		ElementSource result = frontend.varQuery(query, var);
		while (result.hasMoreElements()) {
			Object res = result.nextElement();
			boolean ok = expectedSet.remove(res);
			assertTrue("Unexpected result: "+ res, ok);
		}
		assertTrue("Expected results not found: "+expectedSet,expectedSet.isEmpty());
	}
	
	void test_must_findall(String query, String[] vars, String[][] expected)
	throws ParseException, TypeModeError {
		for (int i = 0; i < vars.length; i++) {
			ElementSource result = frontend.varQuery(query, vars[i]);
			for (int j = 0; j < expected.length; j++) {
				assertTrue(result.hasMoreElements());
				assertEquals(frontend.makeTermFromString(expected[j][i]),
					result.nextElement());
			}
			assertFalse(result.hasMoreElements());
		}
	}
	
	void show_test_results(String query)
	throws ParseException, TypeModeError {
		ElementSource result = frontend.frameQuery(query);
		while (result.hasMoreElements()) {
			System.out.println(result.nextElement());
		}
	}
	
	
	protected void test_resultcount(String query, int numresults) throws ParseException, TypeModeError {
		int counter = get_resultcount(query);
		assertEquals("Expected number of results:",numresults,counter);
	}

	protected int get_resultcount(String query)
		throws ParseException, TypeModeError {
		ElementSource result = frontend.frameQuery(query);
		int counter = 0; 
		while (result.hasMoreElements()) {
			++counter;
			result.nextElement();
		}
		return counter;
	}
	
	protected void deleteDirectory(File dir) {
	    if (dir.isDirectory()) {
	        File[] children = dir.listFiles();
	        for (int i = 0; i < children.length; i++) {
	            deleteDirectory(children[i]);
	        }
	    }  
	    dir.delete();
	}
}

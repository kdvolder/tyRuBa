package tyRuBa.tests;

import java.io.File;

import tyRuBa.engine.BackupFailedException;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.TyRuBaConf;


public class DualFrontendTest extends TyrubaTest {
	
	public DualFrontendTest(String arg0) {
		super(arg0);
	}
	
	protected void setUp() throws Exception {
		super.setUpNoFrontend();
	}
		
	private FrontEnd makeFrontend(String storageLocation) {
		TyRuBaConf conf = new TyRuBaConf();
		conf.setStoragePath(new File(storageLocation));
		return new FrontEnd(conf);
//		true,         new File(storageLocation),getJDBCURL(), true,               null,               true,              false);
//		loadInitFile, File storagePath,         String dbURL, boolean persistent, ProgressMonitor mon,boolean clean,boolean enableBackgroundCleaning) {
			}
	
	public void testAddAndQuery() throws Exception {
		
		// Create and use the first front end:
		
		FrontEnd frontend1 =  makeFrontend("fdb");
		
		frontend = frontend1;
		frontend.parse("test :: String PERSISTENT MODES (F) IS NONDET END");
		frontend.parse("test(Kris).");
		test_must_succeed("test(Kris)");
		test_must_equal("test(?x)", "?x", "Kris");
		test_resultcount("test(?x)", 1);
		
		// Create and use the second frontend
		FrontEnd frontend2 = makeFrontend("fbd2");
		
		frontend = frontend2;
		frontend.parse("test :: Integer PERSISTENT MODES (F) IS NONDET END");
		frontend.parse("test(99).");
		test_must_succeed("test(99)");
		test_must_equal("test(?x)", "?x", "99");
		test_resultcount("test(?x)", 1);
		
		// Now check if first frontend is still working properly
		// in a poor implementation the test table from frontend1 could
		// be dropped and replaced by the one from frontend 2.
		
		frontend = frontend1;
		frontend.parse("test(DeVolder).");
		test_must_succeed("test(Kris)");
		test_must_succeed("test(DeVolder)");
		test_must_findall("test(?x)", "?x", new String[] {"Kris","DeVolder"});
		test_resultcount("test(?x)", 2);
		
	}
	
}

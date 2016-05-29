//package tyRuBa.performancetest;
//
//import java.io.File;
//
//import tyRuBa.engine.FrontEnd;
//import tyRuBa.engine.RubFileBucket;
//import tyRuBa.engine.RuleBase;
//import tyRuBa.engine.TyRuBaConf;
//import tyRuBa.engine.factbase.PersistenceConf;
//import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBConf;
//
//import ca.ubc.performancetest.Task;
//
//public class InsertionTask extends Task {
//
//	private File facts_dir;
//	
//	private FrontEnd frontend;
//
//	private TyRuBaConf conf;
//
//	public InsertionTask(String path, boolean dumpFacts, PersistenceConf persistenceConf) {
//		super("insertion with "+ (dumpFacts?"dumpFacts &&":"")+persistenceConf);
//		conf = new TyRuBaConf();
//		conf.setDumpFacts(dumpFacts);
//		conf.setPersistenceConf(persistenceConf);
//		this.facts_dir = new File(path);
//	}
//	
//	@Override
//	protected void setup() throws Exception {
//		this.frontend = new FrontEnd(conf);
//		RuleBase.silent = true;
//		frontend.load("test_facts/rules/initfile.rub"); // load needed definitions
//		for (File file : facts_dir.listFiles()) {
//			if (file.isDirectory()) {
//				System.err.println("Warning: directory "+file+" skipped.");
//			}
//			else
//				new RubFileBucket(frontend,file.getPath());
//		}
//	}
//
//	@Override
//	protected void teardown() throws Exception {
//		super.teardown();
//		frontend.shutdown();
//		frontend = null;
//	}
//
//
//
//	@Override
//	protected void run() throws Exception {
//		frontend.updateBuckets(); // forces all the buckets to be parsed now
//	}
//
//}

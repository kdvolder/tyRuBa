package tyRuBa.performancetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tyRuBa.engine.factbase.FileBasedPersistence;
import tyRuBa.engine.factbase.PersistenceConf;
import tyRuBa.engine.factbase.PersistenceStrategy;
import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBConf;
import tyRuBa.engine.factbase.hashtable.FileBasedPersistenceConf;
import ca.ubc.performancetest.TaskRunner;

public class PeformanceTester {

	public static void main(String[] args) throws IOException {
		BerkeleyDBConf bdbConf;
		Date runningDate = new Date(System.currentTimeMillis());
		String saveFileName = "perfdata/" + runningDate.toLocaleString();
		
		List<PersistenceConf> persistenceConfs = new ArrayList<PersistenceConf>();
		
//		persistenceConfs.add(new FileBasedPersistenceConf());
//
//		// Lazy open indexes: very fast inserts (but start-up for initial queries)
//		bdbConf = new BerkeleyDBConf();
//		bdbConf.setStoragePath(new File("fdb1"));
//		bdbConf.setLazyOpenIndexes(true);
//		persistenceConfs.add(bdbConf);

//		// Eager open indexes: slow insert (but queries fast right away)
//		// With non-optimized TyRuBaBindings
//		bdbConf = new BerkeleyDBConf();
//		bdbConf.setStoragePath(new File("fdb2"));
//		bdbConf.setLazyOpenIndexes(false);
//		bdbConf.setOptimizedBindings(false);
//		persistenceConfs.add(bdbConf);
		
		// Eager open indexes: slow insert (but queries fast right away)
		// With optimized TyRuBaBindings
		bdbConf = new BerkeleyDBConf();
		bdbConf.setStoragePath(new File("fdb3"));
		bdbConf.setLazyOpenIndexes(false);
		persistenceConfs.add(bdbConf);
		
		TaskRunner r = new TaskRunner(saveFileName);

		for (PersistenceConf persistanceSystem : persistenceConfs) {
			//r.add(new DummyTask(150,persistanceSystem.toString()));
			//r.add(new InsertionTask("test_facts/tyruba/",true,persistanceSystem));
			r.add(new InsertionTask("test_facts/tyruba/",false,persistanceSystem));
		}
		for (int i = 0; i < 5; i++) {
			r.runTasks();
		}
		r.printStats();
		System.exit(0);
	}

}

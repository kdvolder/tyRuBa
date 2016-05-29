package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;

/**
 * An instance of PersistentCounters can be used to safely generate unique
 * counters.
 * 
 * @author kdvolder
 */
public class PersistentCounters {
	
	BerkeleyDBBasedPersistence env;
	
	Database db;
	StoredMap counterMap;
	
	public PersistentCounters(BerkeleyDBBasedPersistence env) throws DatabaseException {
		this.env = env;
		DatabaseConfig cfg = env.newDatabaseConfig();
		db = env.openDatabase("counters", cfg);
		counterMap = new StoredMap(db,TupleBinding.getPrimitiveBinding(String.class),
				TupleBinding.getPrimitiveBinding(Long.class),true);
	}

	abstract class Worker implements TransactionWorker {
		long result = 0;
	}
	
	public long getUnique(final String counterName) throws Exception {
		Worker work = new Worker() {
 			public void doWork() throws Exception {
				Long l = (Long) counterMap.get(counterName);
				if (l!=null) {
					result = l+1;
				}
				counterMap.put(counterName, result);
			}
		};
		env.doTransaction(work);
		return work.result;
	}

}

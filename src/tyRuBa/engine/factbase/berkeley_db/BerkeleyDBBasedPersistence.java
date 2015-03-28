package tyRuBa.engine.factbase.berkeley_db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import annotations.Export;
import annotations.Feature;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.ClassCatalog;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.TransactionRunner;
import com.sleepycat.collections.TransactionWorker;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentStats;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.StatsConfig;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.recovery.NoRootException;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.Validator;
import tyRuBa.engine.factbase.FactBase;
import tyRuBa.engine.factbase.FileBasedValidatorManager;
import tyRuBa.engine.factbase.PersistenceStrategy;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.engine.factbase.hashtable.HashTableFactBase;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.util.Files;

@Feature(names="./BDB")
public class BerkeleyDBBasedPersistence extends PersistenceStrategy {
	
	private static final int DEFAULT_CACHE_PERCENT = 20;

	@Export(to="Validator")
	private File path;
	
	private Environment env = null;
	
	private FrontEnd frontend; 

	public BerkeleyDBBasedPersistence(FrontEnd frontend, BerkeleyDBConf bdbConf) {
		this.bdbConf = bdbConf;
		this.path = bdbConf.getStoragePath();
		this.frontend = frontend;
	}
	
	private Environment getEnv() {
		if (env==null) {
			initialize(true);
		}
		return env;
	}

	List<Database> openDatabases = new ArrayList<Database>();
	
	private TransactionRunner transactionRunner;

	private StoredClassCatalog classCat;
	
	private TyRuBaBindings tyRuBaBindings;
	
	@Feature(names={"./Configure"})
	private boolean isTransactional = false;

	@Feature(names={"./Configure"})
	private BerkeleyDBConf bdbConf;
		
	private void initialize(boolean retry) {
		path.mkdirs();
		EnvironmentConfig cfg = new EnvironmentConfig();
		cfg.setAllowCreate(true);
		cfg.setTransactional(isTransactional);
		cfg.setCachePercent(DEFAULT_CACHE_PERCENT);
		try {
			env = new Environment(path,cfg);
			transactionRunner = new TransactionRunner(env);
		} 
		catch (NoRootException e) {
			if (retry) {
				Files.deleteDirectory(path);
				initialize(false);
			}
			else
				throw new Error(e);
		}
		catch (DatabaseException e) {
			throw new Error(e);
		} 
		// Create/open class catalog
    	DatabaseConfig dbConfig = this.newDatabaseConfig(); 
		dbConfig.setSortedDuplicates(false);
		try {
			Database classCatDB = openDatabase("classes", dbConfig);
			classCat = new StoredClassCatalog(classCatDB);
		} catch (DatabaseException e) {
			throw new Error(e);
		}
		System.out.println("BDB Setup Succesfull...");
		try {
			System.out.println("   cache size = "+env.getConfig().getCacheSize()/(1024*1024.0)+" Mb");
		} catch (DatabaseException ignore) {
		}	
	}

	public void clean() {
		if (env!=null) {
			throw new Error("Clean should not be called after BDB environment was already opened!");
		}
		Files.deleteDirectory(path);
	}

	public void crash() {
		// We can't really simulate a crash so let's just shutdown.
		shutdown();
	}

	public FactBase createFactBase(PredInfo info) {
		return new BerkeleyDBFactBase(this, info);
	}

	public void shutdown() {
		try {
			if (env!=null) {
				if (bdbConf.getDeferredWrite()) {
					for (Database db : openDatabases) {
						db.sync();
					}
				}
				for (Database db : openDatabases) {
					db.close();
				}
				env.close();				
				env = null;
			}
		} catch (DatabaseException ignore) {
			ignore.printStackTrace();
		}
	}

	public void backup() {
		try {
			getEnv().sync();
			if (bdbConf.getDeferredWrite()) {
				for (Database db : openDatabases) {
					db.sync();
				}
			}
		} catch (DatabaseException e) {
			throw new Error(e);
		}
	}

	@Feature(names="Validator")
	public ValidatorManager createValidatorManager() {
		return new FileBasedValidatorManager(path.getPath());
	}

	public Database openDatabase(String databaseName, DatabaseConfig dbConfig) throws DatabaseException {
		Transaction txn = beginTransaction();
    	dbConfig.setTransactional(isTransactional);
		Database db = getEnv().openDatabase(txn, databaseName, dbConfig);
		openDatabases.add(db);
		if (txn!=null) txn.commit();
		return db;
	}

	public Database openSecondaryDatabase(String databaseName, Database primaryDatabase, SecondaryConfig dbConfig) 
	throws DatabaseException {
		Transaction txn = beginTransaction();
    	dbConfig.setTransactional(isTransactional);
		Database db = getEnv().openSecondaryDatabase(txn, databaseName, primaryDatabase, dbConfig);
		openDatabases.add(db);
		if (txn!=null) txn.commit();
		return db;
	}
	
	public void doTransaction(TransactionWorker worker) throws Exception {
		transactionRunner.run(worker);
	}

	public PersistentCounters getPersistentCounters() throws DatabaseException {
		return new PersistentCounters(this);
	}

	public TyRuBaBindings getTyRuBaBindings() {
		if (tyRuBaBindings == null) {
			tyRuBaBindings = bdbConf.getOptimizedBindings()
				?new FastTyRuBaBindings(this)
				:new TyRuBaBindings(this);
		}
		return tyRuBaBindings;
	}

	public ClassCatalog getClassCat() {
		return classCat;
	}

	public Transaction beginTransaction() throws DatabaseException {
		if (isTransactional)
			return getEnv().beginTransaction(null, null);
		else
			return null;
	}

	public FrontEnd getTyRuBaFrontend() {
		return frontend;
	}

	public void removeDatabase(String dbName) throws DatabaseException {
		Transaction txn = beginTransaction();
		try {
			env.removeDatabase(txn, dbName);
		}
		catch (DatabaseNotFoundException ignore) {
		}
		if (txn!=null) txn.commit();
	}

	/**
	 * This method centralizes creation of DataBaseConfig objects
	 * so that they can be created all with similar, consistent
	 * default parameters.
	 */
	public DatabaseConfig newDatabaseConfig() {
		DatabaseConfig dbConf = new DatabaseConfig();
		dbConf.setAllowCreate(true);
		dbConf.setDeferredWrite(bdbConf.getDeferredWrite());
		dbConf.setTransactional(isTransactional);
		return dbConf;
	}

	public SecondaryConfig newSecondaryConfig() {
		SecondaryConfig dbConf = new SecondaryConfig();
		dbConf.setAllowCreate(true);
		dbConf.setDeferredWrite(bdbConf.getDeferredWrite());
		dbConf.setTransactional(isTransactional);
    	dbConf.setAllowPopulate(true);
    	dbConf.setSortedDuplicates(true);
		return dbConf;
	}

	@Feature(names={"./Configure"})
	public boolean getLazyOpenIndexes() {
		return bdbConf.getLazyOpenIndexes();
	}

	@Override
	public boolean canDoFastBackup() {
		return true; // BDB implementation's backup always has acceptable speed.
	}

	@Override
	public long getCacheSize() {
		try {
			return getEnv().getConfig().getCacheSize();
		} catch (DatabaseException e) {
			throw new Error(e);
		}
	}

	@Override
	public void setCacheSize(long cacheSize) {
		try {
			getEnv().getConfig().setCacheSize(cacheSize);
		} catch (IllegalArgumentException e) {
			throw new Error(e);
		} catch (DatabaseException e) {
			throw new Error(e);
		}
	}

	@Override
	public void printStats() {
		// Might want to print some stats here.
	}

	@Override
	public boolean useBFIndexing() {
		return true;
	}

}

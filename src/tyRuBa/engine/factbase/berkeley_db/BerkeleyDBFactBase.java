/*
 * Created on May 5, 2004
 */
package tyRuBa.engine.factbase.berkeley_db;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import tyRuBa.engine.Frame;
import tyRuBa.engine.IValidator;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBFact;
import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.engine.factbase.FactBase;
import tyRuBa.engine.factbase.ValidatorManager;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.BindingMode;
import tyRuBa.modes.BoundComposite;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.tests.ModeSwitchExpressionTest;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;
import tyRuBa.util.pager.FileLocation;
import annotations.Feature;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredKeySet;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryKeyCreator;

/**
 * A FactBase that uses a BerkeleyDB JE Database 
 * {@link http://www.oracle.com/technology/documentation/berkeley-db/je/}
 * as a persistent backing store.
 */
@Feature(names="./BDB")
public class BerkeleyDBFactBase extends FactBase {
	
	private BerkeleyDBBasedPersistence env;
	
	private Database db;

	private EntryBinding dataBinding;
	
	private PredInfo info;
	
    private ValidatorManager validatorMgr;

	private Map<Long,Record> facts; 
	   // All the facts indexed by record_id
	
	private Map<BindingList,StoredMap> indexes; 
		
	private PersistentCounters counters;

	private TupleBinding keyBinding;

	private boolean insertsDone = false;
	
	private long invalidSeen = 0;
	
	private long validSeen = 0;

	/**
	 * Determine whether or not a "cleaning" seems needed. The idea is that
	 * we leave invalidate facts around until it looks like invalid
	 * entries are starting to dominate the retrieved results. 
	 */
	private boolean needsCleaning() {
		return invalidSeen>100 && invalidSeen>validSeen;
	}

	private void validSeen() {
		if (invalidSeen>0) validSeen++;
	}
	
	private void invalidSeen() {
		invalidSeen++;
	}
	
    /**
     * Creates a new BerkeleyDB based Factbase.
     */
    public BerkeleyDBFactBase(BerkeleyDBBasedPersistence env,PredInfo info) {
    	this.info = info;
    	this.env = env;
    	this.validatorMgr = env.getValidatorManager();
    	
    	try {
    		openAdminDBs();
    		openPrimaryDB();
    		openIndexes();
		} catch (DatabaseException e) {
			throw new Error(e);
		}
    	
    }

	private void openPrimaryDB() throws DatabaseException {
		// Create/open primary database
    	DatabaseConfig dbConfig = env.newDatabaseConfig(); 
		dbConfig.setSortedDuplicates(false);
		db = env.openDatabase(info.getPredId().toString(), dbConfig);

		keyBinding = TupleBinding.getPrimitiveBinding(Long.class);
		dataBinding = env.getTyRuBaBindings().getRecordBinding(info.getTypeList());
		// Create the facts StoredMap
		facts = new StoredMap(db,keyBinding,dataBinding,true);
	}

	private void openAdminDBs() throws DatabaseException, IllegalArgumentException {
		// Create/open counter database
		counters = env.getPersistentCounters();
	}

    /**
     * Initializes the indexes. Indexes are always created for the all bound
     * mode and all free mode. In addition, any other modes defined in the
     * predicate declaration have indexes created for them. Any other modes will
     * have their indexes created on the fly.
     * @throws DatabaseException 
     */
	private void openIndexes() throws DatabaseException {
		indexes = new HashMap<BindingList, StoredMap>();
		if (!env.getLazyOpenIndexes()) {
			for (int i = 0; i < info.getNumPredicateMode(); i++) {
				PredicateMode predMode = info.getPredicateModeAt(i);
				if (predMode.getParamModes().isAllBound()) { 
					// All bound index is always created automatically.
					// Do not create it twice!
				}
				else {
					StoredMap index = openIndex(predMode);
					indexes.put(predMode.getParamModes(), index);
				}
			}
		}
	}
 
    /**
     * Gets an index for a given mode.
     * @return 
     * @throws DatabaseException 
     */
	private StoredMap getIndex(PredicateMode mode) {
        BindingList paramModes = mode.getParamModes();
		StoredMap index = indexes.get(paramModes);
        if (index == null) {
            try {
				index = openIndex(mode);
			} catch (DatabaseException e) {
				throw new Error(e);
			}
            indexes.put(paramModes, index);
        }
        return index;
    }

	/**
     * This method encapsulates the naming convention used for naming
     * secondary index databases.
     */
	private String indexDBName(BindingList list) {
		return info.getPredId()+":"+list.getBFString();
	}

	/**
     * @see tyRuBa.engine.factbase.FactBase#isEmpty()
     */
    public boolean isEmpty() {
        return facts.isEmpty();
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#isPersistent()
     */
    public boolean isPersistent() {
        return info.isPersistent();
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#insert(tyRuBa.engine.RBComponent)
     */
    public void insert(RBComponent f) {
		if (!f.isGroundFact())
			throw new Error("Only ground facts can be inserted into BDB-based persistent factbases");
		if (f.isValid())
			try {
				insertsDone = true;
				facts.put(counters.getUnique("OID"),
						new Record(f.getArgs(),f.getValidator().handle()));
			} catch (Exception e) {
				throw new Error(e);
			}
    }

    /**
     * @see tyRuBa.engine.factbase.FactBase#compile(tyRuBa.modes.PredicateMode,
     * tyRuBa.engine.compilation.CompilationContext)
     */
	public Compiled basicCompile(PredicateMode mode, CompilationContext context) {
		if (mode.getParamModes().usePartialKeyExtraction()) {
			return compileWithPartialKey(mode,context);
		}
		else {
			final StoredMap index = getIndex(mode);
			final int[] boundIdxs = mode.getParamModes().getBoundIndexes();
			if (mode.getMode().hi.compareTo(Multiplicity.one) <= 0) {
				return new SemiDetCompiled(mode.getMode()) {

					public Frame runSemiDet(Object input, RBContext context) {
						if (needsCleaning()) removeInvalids();
						final RBTuple goal = (RBTuple) input;
						final RBTuple key = goal.project(boundIdxs);
						Frame result = null;
						Iterator iter = index.duplicates(key).iterator();
						boolean hasNext = iter.hasNext();
						while (hasNext) {
							Record entry = (Record) iter.next();
							hasNext = iter.hasNext();
							RBTuple fact = entry.data;
							IValidator v = validatorMgr.get(entry.handle);
							if (v.isValid()) {
								validSeen();
								result = fact.unify(goal, new Frame());
							}
							else {
								// iter.remove(); This doesn't work because a secondary database is read-only
								invalidSeen();
							}
							if (result!=null)
								return result;
						}
						return result;
					}

				};
			} 
			else {
				return new Compiled(mode.getMode()) {
					public ElementSource runNonDet(Object input, RBContext context) {
						if (needsCleaning()) removeInvalids();
						final RBTuple goal = (RBTuple) input;
						final RBTuple key = goal.project(boundIdxs);
						return ElementSource.with(index.duplicates(key).iterator()).map(new Action() {
							public Object compute(Object o_entry) {
								Record entry = (Record) o_entry;
								RBTuple fact = entry.data;
								IValidator v = validatorMgr.get(entry.handle);
								if (v.isValid()) {
									validSeen();
									return goal.unify(fact, new Frame());
								}
								else {
									invalidSeen();
									return null;
								}
							}
						});
					}

				};
			}
		}
	}
	
	@Feature(names="./partialKey")
	private Compiled compileWithPartialKey(PredicateMode mode,
			CompilationContext context) {
		final StoredMap index = getIndex(mode);
		final BindingList boundIdxs = mode.getParamModes();
		if (mode.getMode().hi.compareTo(Multiplicity.one) <= 0) {
			return new SemiDetCompiled(mode.getMode()) {

				public Frame runSemiDet(Object input, RBContext context) {
					if (needsCleaning()) removeInvalids();
					final RBTuple goal = (RBTuple) input;
					final RBTerm key = goal.project(boundIdxs);
					Frame result = null;
					Iterator iter = index.duplicates(key).iterator();
					boolean hasNext = iter.hasNext();
					while (hasNext) {
						Record entry = (Record) iter.next();
						hasNext = iter.hasNext();
						RBTuple fact = entry.data;
						IValidator v = validatorMgr.get(entry.handle);
						if (v.isValid()) {
							validSeen();
							result = fact.unify(goal, new Frame());
						}
						else {
							// iter.remove(); This doesn't work because a secondary database is read-only
							invalidSeen();
						}
						if (result!=null)
							return result;
					}
					return result;
				}

			};
		} 
		else {
			return new Compiled(mode.getMode()) {
				public ElementSource runNonDet(Object input, RBContext context) {
					if (needsCleaning()) removeInvalids();
					final RBTuple goal = (RBTuple) input;
					final RBTerm key = goal.project(boundIdxs);
					return ElementSource.with(index.duplicates(key).iterator()).map(new Action() {
						public Object compute(Object o_entry) {
							Record entry = (Record) o_entry;
							RBTuple fact = entry.data;
							IValidator v = validatorMgr.get(entry.handle);
							if (v.isValid()) {
								validSeen();
								return goal.unify(fact, new Frame());
							}
							else {
								invalidSeen();
								return null;
							}
						}
					});
				}

			};
		}
	}

    public void backup() {
		
    }

	/**
	 * Iterate through all elements in the primary database and remove any entries that are invalid
	 */
	private void removeInvalids() {
		try {
			System.err.println("Cleaning factbase "+ db.getDatabaseName());
		} catch (DatabaseException e1) {
		}
		Iterator<Entry<Long, Record>> entries = facts.entrySet().iterator();
		int invalid = 0;
		int valid = 0;
		boolean hasNext = entries.hasNext();
		while (hasNext) {
			Entry<Long, Record> e = entries.next();
			hasNext = entries.hasNext();
			IValidator v = validatorMgr.get(e.getValue().handle);
			if (!v.isValid()) {
				entries.remove();
				invalid++;
			}
			else
				valid++;
		}
		this.validSeen=0;
		this.invalidSeen=0;
		System.err.println("Cleaning done:  removed "+invalid+"/"+(valid+invalid)+" = " + invalid*100/(valid+invalid)+"%");
	}
	
	public TupleType getTypeList() {
		return info.getTypeList();
	}

	public EntryBinding getDataBinding() {
		return dataBinding;
	}

	EntryBinding entryBindingFor(Type type) {
		return env.getTyRuBaBindings().get(type);
	}

	private Database openSecondaryDatabase(SecondaryConfig dbConfig, BindingList list) throws DatabaseException {
		String dbName = indexDBName(list);
		if (insertsDone) {
			// If inserts where done while index was not yet opened
			// then BDB doesn't maintain the index and so it is inconsistent.
			// We will recreate it from scratch
			env.removeDatabase(dbName);
		}
		return env.openSecondaryDatabase(dbName, db, dbConfig);

	}
	
	@Feature(names={"./partialKey"})
	private StoredMap openIndex(PredicateMode predMode) throws DatabaseException {
		BindingList list = predMode.getParamModes();
    	
    	SecondaryConfig dbConfig = env.newSecondaryConfig();
    	dbConfig.setKeyCreator(makeSecondaryKeyCreator(this,list));

		Database secondaryDB = openSecondaryDatabase(dbConfig,list);
		
		return new StoredMap(secondaryDB,
				env.getTyRuBaBindings().get(info.getTypeList().project(list)),
				dataBinding,
				false);
	}

	private SecondaryKeyCreator makeSecondaryKeyCreator(
			BerkeleyDBFactBase berkeleyDBFactBase, BindingList bindings) {
		if (!bindings.usePartialKeyExtraction())
			return new RBIndexedSecondaryKeyCreator(berkeleyDBFactBase,bindings);
		else 
			return new RBPartialSecondaryKeyCreator(berkeleyDBFactBase,bindings);
	}
    	
}
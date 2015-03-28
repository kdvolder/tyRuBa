//package tyRuBa.engine.factbase.berkeley_db;
//
//import java.util.Map;
//
//import com.sleepycat.bind.EntryBinding;
//import com.sleepycat.bind.serial.SerialBinding;
//import com.sleepycat.bind.tuple.TupleBinding;
//import com.sleepycat.bind.tuple.TupleInput;
//import com.sleepycat.bind.tuple.TupleOutput;
//import com.sleepycat.collections.StoredMap;
//import com.sleepycat.je.Database;
//import com.sleepycat.je.DatabaseConfig;
//import com.sleepycat.je.DatabaseException;
//
//import annotations.Feature;
//import tyRuBa.engine.IValidator;
//import tyRuBa.engine.RBTerm;
//import tyRuBa.engine.Validator;
//import tyRuBa.engine.factbase.PersistenceStrategy;
//import tyRuBa.engine.factbase.ValidatorManager;
//
//@Feature(names="./BDB")
//public class BDBValidatorManager implements ValidatorManager {
//
//	@Feature(names="./BDB")
//	private static final String VALIDATOR_DB = "ValidatorDB";
//
//	@Feature(names="./BDB")
//	private EntryBinding dataBinding = null;
//	
//	@Feature(names="./BDB")
//	BerkeleyDBBasedPersistence env;
//	
//	@Feature(names="./BDB")
//	Database db;
//	
//	@Feature(names="./BDB")
//	private Map<Long,Validator> validators;
//	
//	@Feature(names="./BDB")
//	public BDBValidatorManager(BerkeleyDBBasedPersistence env) throws DatabaseException {
//		this.env = env;
//		DatabaseConfig cfg = env.newDatabaseConfig();
//		db = env.openDatabase(VALIDATOR_DB, cfg);
//		validators = new StoredMap(db,
//				TupleBinding.getPrimitiveBinding(Long.class),
//				getDataBinding(),
//				true);
//	}
//
//	@Feature(names="./BDB")
//	private EntryBinding getDataBinding() {
//		if (dataBinding==null)
//			dataBinding = new TyRuBaSerialBinding(env.getClassCat(),RBTerm.class);
//		return dataBinding;
//	}
//
//	@Feature(names="./BDB")
//	public void backup() {
//		// Doesn't need to do anything. Validators are stored in BDB so get
//		// backed up automatically when BDB is backed up.
//	}
//
//	@Feature(names="./BDB")
//	public IValidator get(long validatorHandle) {
//        Long handle = new Long(validatorHandle);
//        Validator result = (Validator) validators.get(handle);
//        if (result==null)
//        	return Validator.theNeverValid;
//        else
//        	return result;
//	}
//
//	@Feature(names="./BDB")
//	public IValidator get(String identifier) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Feature(names="./BDB")
//	public long getLastInvalidatedTime() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Feature(names="./BDB")
//	public void invalidate(IValidator validator) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Feature(names="./BDB")
//	public IValidator newValidator(String identifier) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Feature(names="./BDB")
//	public void printOutValidators() {
//		// TODO Auto-generated method stub
//	}
//
//	@Feature(names="./BDB")
//	public void setOutdated(IValidator validator, boolean isOutdate) {
//	}
//	
//}

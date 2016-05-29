package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;

import tyRuBa.modes.BindingList;

public class RBIndexedSecondaryKeyCreator extends RBSecondaryKeyCreator {

	private int[] boundIdxs;
	
	private EntryBinding keyBinding;
	
	public boolean createSecondaryKey(SecondaryDatabase secondary,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
			throws DatabaseException {
		
		Record r = (Record) dataBinding.entryToObject(data);
		keyBinding.objectToEntry(r.data.project(boundIdxs),result);
		return true;
	}

	public RBIndexedSecondaryKeyCreator(BerkeleyDBFactBase env,
			BindingList bindings) {
		super(env,bindings);
		this.boundIdxs = bindings.getBoundIndexes();
    	keyBinding = env.entryBindingFor(env.getTypeList().project(bindings));
	}

}

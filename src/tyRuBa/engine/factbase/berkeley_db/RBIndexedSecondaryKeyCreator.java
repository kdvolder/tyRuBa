package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.modes.BindingList;

import annotations.Feature;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class RBIndexedSecondaryKeyCreator extends RBSecondaryKeyCreator {

	@Feature(names="./BDB")
	private int[] boundIdxs;
	
	@Feature(names="./BDB")
	private EntryBinding keyBinding;
	
	@Feature(names="./BDB")
	public boolean createSecondaryKey(SecondaryDatabase secondary,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
			throws DatabaseException {
		
		Record r = (Record) dataBinding.entryToObject(data);
		keyBinding.objectToEntry(r.data.project(boundIdxs),result);
		return true;
	}

	@Feature(names="./BDB")
	public RBIndexedSecondaryKeyCreator(BerkeleyDBFactBase env,
			BindingList bindings) {
		super(env,bindings);
		this.boundIdxs = bindings.getBoundIndexes();
    	keyBinding = env.entryBindingFor(env.getTypeList().project(bindings));
	}

}

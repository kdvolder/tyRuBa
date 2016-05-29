package tyRuBa.engine.factbase.berkeley_db;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;

import tyRuBa.modes.BindingList;

public class RBPartialSecondaryKeyCreator extends RBSecondaryKeyCreator {

	private BindingList bindings;
	private EntryBinding keyBinding;

	public RBPartialSecondaryKeyCreator(BerkeleyDBFactBase env,
			BindingList bindings) {
		super(env,bindings);
		this.bindings = bindings;
    	keyBinding = env.entryBindingFor(env.getTypeList().project(bindings));
	}

	public boolean createSecondaryKey(SecondaryDatabase secondary,
			DatabaseEntry key, DatabaseEntry data, DatabaseEntry result)
			throws DatabaseException {
		
		Record r = (Record) dataBinding.entryToObject(data);
		keyBinding.objectToEntry(r.data.project(bindings),result);
		return true;
	}

}

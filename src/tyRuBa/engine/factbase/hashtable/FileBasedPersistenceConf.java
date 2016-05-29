package tyRuBa.engine.factbase.hashtable;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.factbase.FileBasedPersistence;
import tyRuBa.engine.factbase.PersistenceConf;
import tyRuBa.engine.factbase.PersistenceStrategy;

public class FileBasedPersistenceConf extends PersistenceConf {
	
	private boolean backgroundCleaning = true;;

	@Override
	public PersistenceStrategy createStrategy(FrontEnd frontend) {
		return new FileBasedPersistence(this);
	}
	
	@Override
	public String toString() {
		return "  persistence system = FileBased " +
		"\n   backgroundCleaning = " + backgroundCleaning;
	}

	public void setBackgroundCleaning(boolean b) {
		if (locked) throw new Error("Configuration is locked");
		backgroundCleaning = b;
	}
	
	public boolean getBackgroundCleaning() {
		return backgroundCleaning;
	}

	public int getDefaultCacheSize() {
		return FileBasedPersistence.defaultPagerCacheSize;
	}
}

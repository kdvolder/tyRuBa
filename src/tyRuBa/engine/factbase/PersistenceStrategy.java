package tyRuBa.engine.factbase;

import tyRuBa.modes.PredInfo;

/**
 * This class is intended to centralizes all "storage" dependent code.
 * 
 * By implementing different sublcasses it should be possible to switch between
 * a file system based store or a JDBC based store.
 */
public abstract class PersistenceStrategy {

	private ValidatorManager validatorManager;

	public abstract void clean();

	public abstract void shutdown();

	public abstract void crash();

	public abstract FactBase createFactBase(PredInfo info);

	public abstract void backup();

	public abstract ValidatorManager createValidatorManager();
	
	public final ValidatorManager getValidatorManager() {
		if (validatorManager == null) {
			validatorManager = createValidatorManager();
		}
		return validatorManager;
	}

	/**
	 * Returns true if a "fast" backup is possible at this time, false
	 * if a backup performed now will likely take a long time.
	 */
	public abstract boolean canDoFastBackup();

	public abstract long getCacheSize();

	public abstract void setCacheSize(long cacheSize);

	public abstract void printStats();

	public boolean useBFIndexing() {
		return false;
	}
	
}

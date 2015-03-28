package tyRuBa.engine.factbase;

import annotations.Export;
import annotations.Feature;
import tyRuBa.engine.Validator;
import tyRuBa.modes.PredInfo;

/**
 * This class is intended to centralizes all "storage" dependent code.
 * 
 * By implementing different sublcasses it should be possible to switch between
 * a file system based store or a JDBC based store.
 * 
 * @category JDBC
 */
public abstract class PersistenceStrategy {

	private ValidatorManager validatorManager;

	@Export(to="./BDB")
	public abstract void clean();

	@Export(to="./BDB")
	public abstract void shutdown();

	@Export(to="./BDB")
	public abstract void crash();

	@Export(to="./BDB")
	public abstract FactBase createFactBase(PredInfo info);

	@Export(to="./BDB")
	public abstract void backup();

	@Export
	@Feature(names="Validator")
	public abstract ValidatorManager createValidatorManager();
	
	@Export(to="./BDB")
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

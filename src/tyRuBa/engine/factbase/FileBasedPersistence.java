package tyRuBa.engine.factbase;

import java.io.File;

import tyRuBa.engine.factbase.hashtable.FileBasedPersistenceConf;
import tyRuBa.engine.factbase.hashtable.HashTableFactBase;
import tyRuBa.modes.PredInfo;
import tyRuBa.util.Files;
import tyRuBa.util.pager.Pager;

public class FileBasedPersistence extends PersistenceStrategy {

	private File path;
	
    private static Pager pager;
    
	public static final int defaultPagerCacheSize = 5000;
    public static final int defaultPagerQueueSize = 1000;

	public FileBasedPersistence(FileBasedPersistenceConf conf) {
		this.path = conf.getStoragePath();
        if (pager != null) {
            pager.shutdown();
        }
        pager = new Pager(defaultPagerCacheSize,defaultPagerQueueSize, conf.getBackgroundCleaning());
	}

	public void clean() {
		Files.deleteDirectory(path);
	}

	public void crash() {
		pager.crash();
		pager = null;
	}

	public FactBase createFactBase(PredInfo info) {
		return new HashTableFactBase(info);
	}

	public void shutdown() {
        if (pager!=null) pager.backup();
        this.path = null;
	}

	public void backup() {
        pager.backup();
	}

	public ValidatorManager createValidatorManager() {
		return new FileBasedValidatorManager(path.getPath());
	}

	@Override
	public boolean canDoFastBackup() {
		return !pager.isDirty();
	}

	public Pager getPager() {
		return pager;
	}

	@Override
	public long getCacheSize() {
		return pager.getCacheSize();
	}

	@Override
	public void setCacheSize(long cacheSize) {
		pager.setCacheSize(cacheSize);
	}

	@Override
	public void printStats() {
		pager.printStats();
	}

}

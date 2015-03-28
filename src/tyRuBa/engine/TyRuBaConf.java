package tyRuBa.engine;

import java.io.File;

import tyRuBa.engine.factbase.PersistenceConf;
import tyRuBa.engine.factbase.PersistenceStrategy;
import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBConf;
import tyRuBa.engine.factbase.hashtable.FileBasedPersistenceConf;
import tyRuBa.jobs.ProgressMonitor;
import annotations.Feature;

/**
 * An instance of TyRuBaConf provides configuration parameters for creating a TyRuBaFrontend.
 * 
 * @author kdvolder
 */
public class TyRuBaConf {

	private ProgressMonitor progressMonitor = null;

	private boolean cleanStart = true;
	private boolean persistent = true;
	private boolean loadInitFile = true;
	

	@Feature(names={"./Configure"})
	private PersistenceConf persistenceConf;

	private boolean locked = false;

	private boolean dumpFacts = false;
	
	public TyRuBaConf() {
		persistenceConf = // new BerkeleyDBConf(); 
		      new FileBasedPersistenceConf();
	}

	/// Getters
	
	public ProgressMonitor getProgressMonitor() {
		locked = true;
		return progressMonitor;
	}
	public File getStoragePath() {
		locked = true;
		return persistenceConf.getStoragePath();
	}
	public boolean getCleanStart() {
		locked = true;
		return cleanStart;
	}
	public boolean getPersistent() {
		locked = true;
		return persistent;
	}
	public boolean getLoadInitFile() {
		locked = true;
		return loadInitFile;
	}
	public boolean getDumpFacts() {
		return dumpFacts;
	}
	
	public PersistenceStrategy createPersistenceStrategy(FrontEnd frontend) {
		locked = true;
		return persistenceConf.createStrategy(frontend);
	}
	
	/// Setters
	
	public void setProgressMonitor(ProgressMonitor mon) {
		if (locked) throw new Error("Configuration is locked");
		progressMonitor = mon;
	}
	public void setStoragePath(File path) {
		persistenceConf.setStoragePath(path);
	}
	public void setCleanStart(boolean b) {
		if (locked) throw new Error("Configuration is locked");
		cleanStart = b;
	}
	public void setPersistenceConf(PersistenceConf persistenceConf) {
		if (locked) throw new Error("Configuration is locked");
		this.persistenceConf = persistenceConf;
	}	
	public void setLoadInitFile(boolean b) {
		if (locked) throw new Error("Configuration is locked");
		loadInitFile = b;
	}
	public void setDumpFacts(boolean b) {
		// no need to lock this 
		// if (locked) throw new Error("Configuration is locked");
		dumpFacts = b;
	}
	
	/// Display
	
	public String toString() {
		return "   cleanStart = " + cleanStart +
		     "\n   persistent = " + persistent +
		     "\n   loadInitFile = " + loadInitFile +
		     "\n   dumpFacts = " + dumpFacts +
		     "\n"+persistenceConf;
	}

}

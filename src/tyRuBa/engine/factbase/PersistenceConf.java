package tyRuBa.engine.factbase;

import java.io.File;

import tyRuBa.engine.FrontEnd;

/**
 * This class contains the configuration parameters related to the persistence 
 * system that stores facts somewhere (disk, database, ...).
 * <p>
 * This class is abstract because it groups only those parameters that are shared
 * between all PersistenceStrategies. More specific instances override the factory
 * method to determine what type of PersistenceStrategy to employ and may manage
 * specific sets of configuration parameters for those specific strategies.
 * 
 * @author kdvolder
 */
public abstract class PersistenceConf {
	
	/**
	 * This field is used to "lock" the configuration parameters. When they are
	 * locked the parameters can no longer be changed. Parameters become locked
	 * when the first parameter gets retrieved. 
	 * 
	 * This is to ensure that configuration parameters look like they are read-only 
	 * from the point of view of a consumer.
	 */
	protected boolean locked;
	
	private File storagePath = new File("fdb");
	
	public abstract PersistenceStrategy createStrategy(FrontEnd frontend);

	public File getStoragePath() {
		return storagePath;
	}
	
	public void setStoragePath(File storagePath) {
		if (locked) throw new Error("Configurations parameters locked");
		this.storagePath= storagePath;
	}
	
	public String toString() {
		return "   storagePath = "+storagePath;
	}
	
}

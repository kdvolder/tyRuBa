package tyRuBa.engine.factbase.berkeley_db;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.factbase.PersistenceConf;
import tyRuBa.engine.factbase.PersistenceStrategy;

public class BerkeleyDBConf extends PersistenceConf {

	private boolean deferredWrite=true;

	private boolean lazyOpenIndexes=true;
	
	private boolean optimizedBindings=true; 
	  // Turning this off is for testing purposes, so we can measure the
	  // impact of the optimization. There is no good reason to turn this
	  // off otherwise.
	
	public void setDeferredWrite(boolean b) {
		if (locked) throw new Error("Configuration parameters have been locked");
		deferredWrite = b;
	}
	
	public void setLazyOpenIndexes(boolean b) {
		if (locked) throw new Error("Configuration parameters have been locked");
		lazyOpenIndexes = b;
	}

	public void setOptimizedBindings(boolean b) {
		if (locked) throw new Error("Configuration parameters have been locked");
		optimizedBindings = b;
	}
	
	public boolean getDeferredWrite() {
		locked = true;
		return deferredWrite;
	}

	public boolean getLazyOpenIndexes() {
		return lazyOpenIndexes;
	}
	
	public boolean getOptimizedBindings() {
		locked = true;
		return optimizedBindings;
	}

	@Override
	public PersistenceStrategy createStrategy(FrontEnd frontend) {
		return new BerkeleyDBBasedPersistence(frontend, this);
	}

	@Override
	public String toString() {
		return "  persistence system = BDB" +
			 "\n  deferredWrite = " + deferredWrite +
			 "\n  lazyOpenIndexes = " + lazyOpenIndexes + 
			 "\n  optimizedBindings = " + optimizedBindings;
	}

}

package tyRuBa.tests;

import junit.framework.TestCase;
import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBBasedPersistence;
import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBConf;
import tyRuBa.engine.factbase.berkeley_db.PersistentCounters;
import tyRuBa.util.Files;

public class PersistentCountersTest extends TestCase {
	
	public void testGetUnique() throws Exception {
		BerkeleyDBConf conf = new BerkeleyDBConf();
		Files.deleteDirectory(conf.getStoragePath());
		BerkeleyDBBasedPersistence env = new BerkeleyDBBasedPersistence(null,conf);
		PersistentCounters counters = new PersistentCounters(env);
		
		assertEquals(0,counters.getUnique("foo"));
		assertEquals(0,counters.getUnique("bar"));
		assertEquals(1,counters.getUnique("foo"));
		assertEquals(2,counters.getUnique("foo"));
		assertEquals(3,counters.getUnique("foo"));
		assertEquals(1,counters.getUnique("bar"));
		assertEquals(2,counters.getUnique("bar"));
		env.shutdown();
	}

	public void testPersist() throws Exception {
		BerkeleyDBConf conf = new BerkeleyDBConf();
		Files.deleteDirectory(conf.getStoragePath());
		BerkeleyDBBasedPersistence env = new BerkeleyDBBasedPersistence(null,conf);
		PersistentCounters counters = new PersistentCounters(env);
		
		assertEquals(0,counters.getUnique("foo"));
		assertEquals(0,counters.getUnique("bar"));
		assertEquals(1,counters.getUnique("foo"));
		assertEquals(2,counters.getUnique("foo"));
		
		env.shutdown();
		
		env = new BerkeleyDBBasedPersistence(null,conf);
		counters = new PersistentCounters(env);
		
		assertEquals(3,counters.getUnique("foo"));
		assertEquals(1,counters.getUnique("bar"));
		assertEquals(2,counters.getUnique("bar"));
		env.shutdown();
	}
}

package tyRuBa.tests;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

public class MemoryLeakTest extends TyrubaTest {

	public MemoryLeakTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLeaky() throws Exception {
		
//		frontend.parse("test :: String " +
//				"PERSISTENT MODES (F) IS NONDET END");
//		frontend.parse("test(HelloWorld).");

		long mem0 = 0;
		
		for (int i = 0; i < 10; i++) {
//			test_must_succeed("test(HelloWorld)");
//			test_must_equal("test(?x)","?x","HelloWorld");
			frontend.shutdown();
			frontend = null;
			System.gc(); System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc(); System.gc();
			long mem = (Runtime.getRuntime().totalMemory() -
				      Runtime.getRuntime().freeMemory());
			if (mem0==0) mem0 = mem;
			mem = mem - mem0;
			//results[i] = mem; 
			System.out.println("Mem = " + mem );
			setUp(false);
			frontend.parse("test :: String " +
			"PERSISTENT MODES (F) IS NONDET END");
		}

//		for (int i = 0; i < results.length; i++) {
//			System.out.println("mem["+i+"] = "+results[i]);
//		}
	}
	
}

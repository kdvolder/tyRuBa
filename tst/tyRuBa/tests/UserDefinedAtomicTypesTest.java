/*
 * Created on Mar 2, 2005
 */
package tyRuBa.tests;

import org.junit.Before;
import org.junit.Test;

import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;

/**
 * @author kdvolder
 */
public class UserDefinedAtomicTypesTest extends TyrubaJUnit4Test {

	@Before @Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test public void testUnification() throws ParseException, TypeModeError {
		frontend.parse("TYPE Foo AS String");
		frontend.parse(
				"foo2string :: Foo, String \n" +
				"MODES (B,F) IS DET (F,B) IS SEMIDET END");
		
		frontend.parse("foo2string(?s::Foo,?s).");

		test_must_succeed("foo2string(abc::Foo,abc)");
		test_must_fail("foo2string(abc::Foo,ab)");
		test_must_equal("foo2string(abc::Foo,?x)", "?x", "abc");
		test_must_equal("foo2string(?x::Foo,abc)", "?x", "abc");
		test_must_equal("foo2string(?x,abc)", "?x", "abc::Foo");
        test_must_equal("member(?s, [123, abc]), foo2string(?x, ?s)", "?x", "abc::Foo");
	}
	
}

package tyRuBa.tests;

import org.junit.Before;
import org.junit.Test;

public class NoInitFilesTest extends TyrubaJUnit4Test {

	@Before
	@Override public void setUp() throws Exception {
		TyrubaJUnit4Test.initfile = false;
		super.setUp();
	}

	@Test public void typeAsPredicate() throws Exception {
		test_must_succeed("Integer(5)");;
	}

	@Test public void abstractUserdefineTypeAsPredicate() throws Exception {
		frontend.parse("TYPE List<?element> = NonEmptyList<?element> | EmptyList<>");
		frontend.parse("TYPE NonEmptyList<?element> AS <?element,List<?element>>");
		frontend.parse("TYPE EmptyList<> AS <>");
		
		test_must_succeed("NonEmptyList(NonEmptyList<1, EmptyList<>>)");
		test_must_succeed("EmptyList(EmptyList<>)");

		test_must_succeed("List(EmptyList<>)");
		test_must_succeed("List(NonEmptyList<1, EmptyList<>>)");
	}
}

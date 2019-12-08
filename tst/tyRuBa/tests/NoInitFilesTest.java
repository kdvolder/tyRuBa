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

    @Test public void append() throws Exception {
        frontend.parse(
                "List :: Object\n" + 
                "List([]).\n" + 
                "List([?l|?ist]).\n"
        );
        frontend.parse(
                "append :: [?t], [?t], [?t]\n" + 
                "MODES\n" + 
                "(B,B,F) IS DET\n" + 
                "(B,F,B) IS SEMIDET\n" + 
                "(F,B,B) REALLY IS SEMIDET\n" + 
                "(F,F,B) IS MULTI\n" + 
                "END\n"
        );
        frontend.parse(
                "append([],?x,?x) :- List(?x)."
        );
        frontend.parse(
                "append([?x|?xs],?ys,[?x|?zs]) :- append(?xs,?ys,?zs)."
        );
    }
    
}

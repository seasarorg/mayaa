package org.seasar.mayaa.functional.layout;

import java.io.IOException;

import org.junit.Test;
import org.seasar.mayaa.functional.EngineTestBase;


public class LayoutTest extends EngineTestBase {

    @Test
    public void basepage1() throws IOException {
        final String DIR = "/it-case/layout/basepage1/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void basepage2() throws IOException {
        final String DIR = "/it-case/layout/basepage2/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void usecomponent() throws IOException {
        final String DIR = "/it-case/layout/usecomponent/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}

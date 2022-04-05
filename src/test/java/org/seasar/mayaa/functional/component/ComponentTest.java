package org.seasar.mayaa.functional.component;

import java.io.IOException;

import org.junit.Test;
import org.seasar.mayaa.functional.EngineTestBase;

public class ComponentTest extends EngineTestBase {

    @Test
    public void binding() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/binding/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void binding_recursive() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/binding_recursive/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void component1() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/component1/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void component1_client() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/component1_client/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void component2_client() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/component2_client/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void component3_client() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/component3_client/relative/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void component123_client() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/component123_client/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void recursive() throws IOException {
        // enableDump();
        final String DIR = "/it-case/component/recursive/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}

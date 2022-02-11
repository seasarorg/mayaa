package org.seasar.mayaa.functional.customtag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.seasar.mayaa.functional.EngineTestBase;


public class CustomTag extends EngineTestBase {

    @Test
    public void dynamicAttribute() throws IOException {
        final String DIR = "/it-case/customtag/dynamic_attribute/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void dynamicAttributeWrong() throws IOException {
        final String DIR = "/it-case/customtag/dynamic_attribute_wrong/";
        try {
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
            fail();
        } catch (java.lang.IllegalArgumentException e) {
            assertEquals("org.seasar.mayaa.test.tag.DynamicAttributeNotSupportTag does not implements javax.servlet.jsp.tagext.DynamicAttributes.", e.getMessage());
        }
    }

    @Test
    public void emptybody() throws IOException {
        final String DIR = "/it-case/customtag/emptybody/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void eval_body_buffered() throws IOException {
        final String DIR = "/it-case/customtag/eval_body_buffered/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void replace_injection_attribute() throws IOException {
        final String DIR = "/it-case/customtag/replace_injection_attribute/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void rtexprtest() throws IOException {
        final String DIR = "/it-case/customtag/rtexprtest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void scopetest() throws IOException {
        final String DIR = "/it-case/customtag/scopetest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simple_dynamic_test() throws IOException {
        final String DIR = "/it-case/customtag/simple_dynamic_test/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simplebodytest_component() throws IOException {
        final String DIR = "/it-case/customtag/simplebodytest-component/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simplebodytest() throws IOException {
        final String DIR = "/it-case/customtag/simplebodytest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void simpletest() throws IOException {
        final String DIR = "/it-case/customtag/simpletest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void vinulltest() throws IOException {
        final String DIR = "/it-case/customtag/vinulltest/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}

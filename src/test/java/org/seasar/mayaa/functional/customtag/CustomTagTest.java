/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.functional.customtag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.functional.EngineTestBase;


public class CustomTagTest extends EngineTestBase {

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
            assertEquals("org.seasar.mayaa.test.tag.DynamicAttributeNotSupportTag does not implements jakarta.servlet.jsp.tagext.DynamicAttributes.", e.getMessage());
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

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

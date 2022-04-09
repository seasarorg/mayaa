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

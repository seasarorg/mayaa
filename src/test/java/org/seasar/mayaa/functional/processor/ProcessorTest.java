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
package org.seasar.mayaa.functional.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.builder.library.ExpectedTypeMismatchValueException;
import org.seasar.mayaa.impl.engine.processor.TooManyLoopException;


public class ProcessorTest extends EngineTestBase {

    @Test
    public void comment() throws IOException {
        final String DIR = "/it-case/processor/comment/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void echo() throws IOException {
        final String DIR = "/it-case/processor/echo/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void element() throws IOException {
        final String DIR = "/it-case/processor/element/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void exec() throws IOException {
        final String DIR = "/it-case/processor/exec/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void for_() throws IOException {
        final String DIR = "/it-case/processor/for/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void for_toomany() throws IOException {
        final String DIR = "/it-case/processor/for_toomany/";
        try {
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
            fail("TooManyLoopException must be thrown.");
        } catch (TooManyLoopException e) {
        } catch (Throwable e) {
            fail("TooManyLoopException must be thrown.");
        }
    }

    @Test
    public void forEach() throws IOException {
        final String DIR = "/it-case/processor/forEach/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void forEachRecursive() throws IOException {
        final String DIR = "/it-case/processor/forEachRecursive/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void formatDate() throws IOException {
        final String DIR = "/it-case/processor/formatDate/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void formatNumber() throws IOException {
        final String DIR = "/it-case/processor/formatNumber/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void if_() throws IOException {
        final String DIR = "/it-case/processor/if/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void if_property_error() throws IOException {
        final String DIR = "/it-case/processor/if_property_error/";
        try {
            execAndVerify(DIR + "target.html", DIR + "expected.html", null);
            fail();
        } catch (ExpectedTypeMismatchValueException e) {
            assertEquals("value is mismatch for boolean - http://mayaa.seasar.org: if.test, /it-case/processor/if_property_error/target.mayaa#14.", e.getMessage());
        }
    }

    @Test
    public void write() throws IOException {
        final String DIR = "/it-case/processor/write/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }


}

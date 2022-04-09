package org.seasar.mayaa.functional.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
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
            fail();
        } catch (TooManyLoopException e) {
            assertEquals("Too many loops, max count is 2. /it-case/processor/for_toomany/target.mayaa#5.", e.getMessage());
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

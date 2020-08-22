package org.seasar.mayaa.functional;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.seasar.mayaa.impl.builder.library.ExpectedTypeMismatchValueException;
import org.seasar.mayaa.impl.engine.processor.DoRenderNotFoundException;
import org.seasar.mayaa.impl.engine.processor.TooManyLoopException;
import org.springframework.mock.web.MockHttpServletRequest;

public class ProcessorTest extends EngineTestBase {
    static final String BASE_PATH = "/it-case/html-transform/processors/";

    @Test
    public void プロセッサforで要素を繰り返し出力する() throws IOException {
        execAndVerify(BASE_PATH + "for/target.html", BASE_PATH + "for/expected.html", null);
    }

    @Test
    public void プロセッサforでMaxに指定した回数を超えるとTooManyLoopException() throws IOException {
        try {
            // When
            final MockHttpServletRequest request = createRequest(BASE_PATH + "for/target_exception.html");
            exec(request, null);
            fail("TooManyLoopExceptionが発生するはず");
        } catch (TooManyLoopException e) {
            ;
        }
    }

    @Test
    public void プロセッサforeachで要素を繰り返し出力する() throws IOException {
        execAndVerify(BASE_PATH + "foreach/target.html", BASE_PATH + "foreach/expected.html", null);
    }

    @Test
    public void プロセッサdoRenderでコンポーネントを出力する() throws IOException {
        execAndVerify(BASE_PATH + "component/hello.html", BASE_PATH + "component/hello_expected.html", null);
    }

    @Test
    public void プロセッサdoRenderで指定したコンポーネントが存在しない() throws IOException {
        try {
            // When
            final MockHttpServletRequest request = createRequest(BASE_PATH + "component/hello_nocomponent.html");
            exec(request, null);
            fail("DoRenderNotFoundExceptionが発生するはず");
        } catch (DoRenderNotFoundException e) {
            ;
        }

    }

    @Test
    public void プロセッサifで子要素の出力制御を行う() throws IOException {
        execAndVerify(BASE_PATH + "if/target.html", BASE_PATH + "if/expected.html", null);
    }

    @Test
    public void プロセッサifのtestに文字列型の値を指定すると型指定例外をスロー() throws IOException {
        try {
            // When
            final MockHttpServletRequest request = createRequest(BASE_PATH + "if/target_typemismatch.html");
            exec(request, null);
            fail("ExpectedTypeMismatchValueExceptionが発生するはず");
        } catch (ExpectedTypeMismatchValueException e) {
            ;
        }

    }

    @Test
    public void プロセッサwriteで値を出力する() throws IOException {
        execAndVerify(BASE_PATH + "write/target.html", BASE_PATH + "write/expected.html", null);
    }
}

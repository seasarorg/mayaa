package org.seasar.mayaa.functional;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class EngineTest extends EngineTestBase {

    @Test
    public void 単純ケース() throws IOException {
        // Given
        getServletContext().setAttribute("s0", "sv0");

        MockHttpServletRequest request = createRequest("/it-case/engine/test.html");
        request.setAttribute("p0", "v0");

        // When
        MockHttpServletResponse response = exec(request, null);
        printTree();

        // Then
        verifyResponse(response, "/it-case/engine/expected.html");
    }

    @Test
    public void HTMLで指定したCharsetがレスポンスヘッダに設定される() throws IOException {
        execAndVerify("/it-case/html-transform/charset/target.html", "/it-case/html-transform/charset/expected.html", null);
    }

    @Test
    @Ignore
    public void HTML5形式で指定したCharsetがレスポンスヘッダに設定される() throws IOException {
        // Mayaaは <meta charset="XX"> の形式の記述には対応していないので ＠Ignore する。
        execAndVerify("/it-case/html-transform/charset-html5/target.html", "/it-case/html-transform/charset-html5/expected.html", null);
    }


    @Test
    public void スクリプト実行_Javaクラス参照() throws IOException {
        System.setProperty("test.value", "TEST VALUE");
        execAndVerify("/it-case/script/java-bridge/target.html", "/it-case/script/java-bridge/expected.html", null);
    }

}

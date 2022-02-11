package org.seasar.mayaa.functional;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class EngineTest extends EngineTestBase {

    /** 
     * <ul>
     * <li>DOCTYPE宣言をそのまま出力する</li>
     * <li>HTML内の文字実体参照はそのまま出力する</li>
     * </il>
     */
    @Test
    public void HTMLをパースして変更がない部分はそのまま出力する() throws IOException {
        // enableDump();
        final String DIR = "/it-case/html-transform/through/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void 文字実体参照の挙動の確認_tests_1_08() throws IOException {
        final String DIR = "/it-case/html-transform/escape/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    public void 単純ケース() throws IOException {
        // Given
        getServletContext().setAttribute("s0", "sv0");

        MockHttpServletRequest request = createRequest("/it-case/engine/test.html");
        request.setAttribute("p0", "v0");

        // When
        MockHttpServletResponse response = exec(request, null);

        // Then
        verifyResponse(response, "/it-case/engine/expected.html");
    }

    @Test
    public void HTMLで指定したCharsetがレスポンスヘッダに設定される() throws IOException {
        final String DIR = "/it-case/html-transform/charset/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @Test
    @Ignore
    public void HTML5形式で指定したCharsetがレスポンスヘッダに設定される() throws IOException {
        // Mayaaは <meta charset="XX"> の形式の記述には対応していないので ＠Ignore する。
        final String DIR = "/it-case/html-transform/charset-html5/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }


    @Test
    public void スクリプト実行_Javaクラス参照() throws IOException {
        System.setProperty("test.value", "TEST VALUE");

        final String DIR = "/it-case/script/java-bridge/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}

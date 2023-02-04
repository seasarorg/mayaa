package org.seasar.mayaa.regressions.issue13;

import java.io.IOException;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.seasar.mayaa.functional.EngineTestBase;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * https://github.com/seasarorg/mayaa/issues/13 のテストコード
 * 
 * a.mayaa と b.mayaa で ${value} をプロセッサに設置していることが原因。
 * a > b > a の順、あるいは b > a > b の順で実行すると再現する。
 * 
 * 比較として c.mayaa では ${value} を使用しないようにすると
 * c > b > c や b > c > b では発生しない。
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Issue13ReproductionTest extends EngineTestBase {
    @Test
    @Order(1)
    public void 単独初回だとOK_a() throws IOException {
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/a.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/a-expected.html");
    }

    @Test
    @Order(3)
    public void 単独初回だとOK_b() throws IOException {
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/b.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");
    }

    @Test
    @Order(2)
    public void 変数を定義していなけれは発生しない() throws IOException {
        // １回め
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/c.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/c-expected.html");

        // ２回め
        request = createRequest("/it-case/regression/issue13/b.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");

        // 3回め (例外発生せず)
        request = createRequest("/it-case/regression/issue13/c.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/c-expected.html");
    }

    @Test
    @Order(2)
    public void 変数を定義していなけれは発生しない_逆順() throws IOException {
        // １回め
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/b.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");

        // ２回め
        request = createRequest("/it-case/regression/issue13/c.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/c-expected.html");

        // 3回め (例外発生せず)
        request = createRequest("/it-case/regression/issue13/b.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");
    }

    /**
     * このメソッドだけ単独で実行すると発生しない。クラス全体で実行するとこれまでに a > b > b > b > b と実行済みのため
     * a を実行したタイミングで発生する。
     * @throws IOException
     */
    @Test
    @Order(5)
    public void aとbが1回ずつなら発生しない() throws IOException {
        // When
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/a.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/a-expected.html");

        // When
        request = createRequest("/it-case/regression/issue13/b.html");
        response = exec(request, null);

        // Then
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");
    }

    @Test
    @Order(6)
    public void aとbが1回ずつ実行された後再度aを実行すると例外() throws IOException {
        // １回め
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/a.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/a-expected.html");

        // ２回め
        request = createRequest("/it-case/regression/issue13/b.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");

        // 3回め
        request = createRequest("/it-case/regression/issue13/a.html");

        // 実行時点で発生
        response = exec(request, null);
    }

    @Test
    @Order(7)
    public void bとaが1回ずつ実行された後再度bを実行すると例外() throws IOException {
        // １回め
        MockHttpServletRequest request = createRequest("/it-case/regression/issue13/b.html");
        MockHttpServletResponse response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/b-expected.html");

        // ２回め
        request = createRequest("/it-case/regression/issue13/a.html");
        response = exec(request, null);
        verifyResponse(response, "/it-case/regression/issue13/a-expected.html");

        // 3回め
        request = createRequest("/it-case/regression/issue13/b.html");

        // 実行時点で発生
        response = exec(request, null);
    }
}

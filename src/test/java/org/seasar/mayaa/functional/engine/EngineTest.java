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
package org.seasar.mayaa.functional.engine;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.TemplateBuilderImpl;
import org.seasar.mayaa.impl.builder.library.NoRequiredPropertyException;
import org.seasar.mayaa.impl.engine.error.TemplateErrorHandler;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class EngineTest extends EngineTestBase {

    void setUseNewParser(boolean useNewParser) {
        getServiceProvider().getTemplateBuilder().setParameter(TemplateBuilderImpl.USE_NEW_PARSER, Boolean.toString(useNewParser));
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void beforeRender(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/beforeRender/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void 単純ケース(boolean useNewParser)throws IOException {
        // Given
        getServletContext().setAttribute("s0", "sv0");

        MockHttpServletRequest request = createRequest("/it-case/engine/simple/target.html");
        request.setAttribute("p0", "v0");

        // When
        MockHttpServletResponse response = exec(request, null);

        // Then
        verifyResponse(response, "/it-case/engine/simple/expected.html");
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void HTMLで指定したCharsetがレスポンスヘッダに設定される(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/html-transform/charset/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    @Disabled
    public void HTML5形式で指定したCharsetがレスポンスヘッダに設定される(boolean useNewParser)throws IOException {
        // Mayaaは <meta charset="XX"> の形式の記述には対応していないので ＠Ignore する。
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/html-transform/charset-html5/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }


    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void スクリプト実行_Javaクラス参照(boolean useNewParser)throws IOException {
        System.setProperty("test.value", "TEST VALUE");

        setUseNewParser(useNewParser);
        final String DIR = "/it-case/script/java-bridge/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void cdata(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/cdata/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void charset_html(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/charset_html/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void charset_xhtml(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/charset_xhtml/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    /**
     * 処理内で例外が発生した時にErrorHandlerによってキャッチし、
     * java.lang.Throwable.mayaa にてエラーページにフォワードさせる。
     * @throws IOException
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void error_forward(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/error_forward/";

        ErrorHandler errorHandler = new TemplateErrorHandler();
        errorHandler.setParameter("folder", DIR);
        errorHandler.setParameter("extension", "html");
        setErrorHandler(errorHandler);

        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }


    /**
     * 処理内で例外が発生した時にErrorHandlerによってキャッチし、
     * java.lang.Throwable.mayaa にてエラーページにリダイレクトさせる。
     * @throws IOException
     */
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void error_redirect(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/error_redirect/";

        ErrorHandler errorHandler = new TemplateErrorHandler();
        errorHandler.setParameter("folder", DIR);
        errorHandler.setParameter("extension", "html");
        setErrorHandler(errorHandler);

        MockHttpServletRequest request = createRequest(DIR + "target.html");

        // When
        MockHttpServletResponse response = exec(request, null);

        assertEquals(302, response.getStatus());
        assertEquals("/it-case/engine/error_redirect/redirected.html?message=error_redirected&title=tests_1_18", response.getHeader("Location"));
    }
    
    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void escape_html(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/escape/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void escape_xhtml(boolean useNewParser)throws IOException {
        // getServiceProvider().getTemplateBuilder();
        setEngineParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*\\.(html|xml|xhtml)");

        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/escape_xhtml/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void escape_xml(boolean useNewParser)throws IOException {
        setEngineParameter(CONST_IMPL.TEMPLATE_PATH_PATTERN, ".*\\.(html|xml|xhtml)");

        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/escape/";
        execAndVerify(DIR + "target.xml", DIR + "expected.xml", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void forward(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/forward/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void ignore_mayaa(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/ignore_mayaa/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_include(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_include/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_mayaa(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_mayaa/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_no(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_no/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_target_is_empty(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_target_is_empty/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_template(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_template/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void inject_xpath(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/inject_xpath/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void no_xmlns(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/no_xmlns/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void redirect(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/redirect/";
        MockHttpServletRequest request = createRequest(DIR + "target.html");

        // When
        MockHttpServletResponse response = exec(request, null);

        assertEquals(302, response.getStatus());
        assertEquals("/it-case/engine/redirect/redirected.html?message=redirected&title=tests_1_16", response.getHeader("Location"));
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void replace(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/replace/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void required(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/required/";
        MockHttpServletRequest request = createRequest(DIR + "target.html");

        // When
        try {
            exec(request, null);
            fail("NoRequiredPropertyException must be thrown.");
        } catch (NoRequiredPropertyException e) {
        } catch (Throwable e) {
            fail("NoRequiredPropertyException must be thrown.");
        }
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void required_not_empty(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/required_not_empty/";
        MockHttpServletRequest request = createRequest(DIR + "target.html");

        // When
        try {
            exec(request, null);
            fail("NoRequiredPropertyException must be thrown.");
        } catch (NoRequiredPropertyException e) {
        } catch (Throwable e) {
            fail("NoRequiredPropertyException must be thrown.");
        }
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void script(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/script/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void template_attribute(boolean useNewParser)throws IOException {
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/template_attribute/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void undefined_identifier(boolean useNewParser)throws IOException {
        enableDump();
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/undefined_identifier/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

    @ParameterizedTest(name = "useNewParser {0}")
    @ValueSource(booleans = {false, true})
    public void xout(boolean useNewParser) throws IOException {
        setUseNewParser(useNewParser);
        setUseNewParser(useNewParser);
        final String DIR = "/it-case/engine/xout/";
        execAndVerify(DIR + "target.html", DIR + "expected.html", null);
    }

}

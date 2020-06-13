/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.LogManager;

import org.junit.Before;
import org.junit.BeforeClass;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.SourceHolderFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class EngineTestBase {

    private MockServletContext servletContext;
    private Engine engine;

    @BeforeClass
    public static void init() throws SecurityException, IOException {
        try (InputStream in = EngineTestBase.class.getClassLoader().getResourceAsStream("jul.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        }

        SourceHolderFactory.release();
        FactoryFactory.release();
        FactoryFactory.setInstance(new FactoryFactoryImpl());
    }

    @Before
    public void setup() throws SecurityException, IOException {
        final Object _testClassInstance = this;
        servletContext = new MockServletContext() {
            /**
             * {@link ServletContext#getResource(String)}の代替実装。
             * テストクラスのパッケージからの相対パスとして取得する。
             * 
             * @param path 要求されているリソースのパス
             */
            public URL getResource(String path) throws MalformedURLException {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                return _testClassInstance.getClass().getResource(path);
            }
        };

        FactoryFactory.setContext(servletContext);

        engine = ProviderUtil.getEngine();
        // engine.setParameter(EngineImpl.DUMP_ENABLED, "true");
        // engine.setParameter(EngineImpl.PAGE_SERIALIZE, "true");
    }

    /**
     * テスト実行に使用するリクエストオブジェクトを作成する
     * 
     * @param path 処理するHTMLファイルへのパス（クラスパスルートからのパス）
     * @return リクエストオブジェクト（モック）
     */
    protected MockHttpServletRequest createRequest(final String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath(path);
        return request;
    }

    /**
     * テストの実行に設定されているServletContextを取得する
     * 
     * @return ServletContextオブジェクト
     */
    protected MockServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 指定したHTMLファイルに対してEngineの本体処理を呼び出す
     * 
     * @param request            モック化されたリクエストオブジェクト
     * @param pageScopeAttribute あらかじめページスコープに定義済のものとして引き渡す属性のマップ
     * @return レスポンスオブジェクト（モック）
     */
    protected MockHttpServletResponse exec(final MockHttpServletRequest request, final Map<String, Object> pageScopeAttribute) {
        final MockHttpServletResponse response = new MockHttpServletResponse();

        CycleUtil.initialize(request, response);
        engine.doService(pageScopeAttribute, true);

        // verify(servletContext).getRealPath(path);

        return response;
    }

    /**
     * Engineの処理結果として期待する内容に合致しているかを検証する。
     * 
     * @param expectedContentPath 機体結果の内容が保管されているファイルへのパス（クラスパスルートからのパス）
     * @throws IOException IOエラーが発生した場合
     */
    protected void verifyResponse(final MockHttpServletResponse response, final String expectedContentPath) throws IOException {

        final URL url = getClass().getResource(expectedContentPath);
        if (url == null) {
            fail("Specified file is not found. " + expectedContentPath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    line = reader.readLine();
                    break;
                }
                if (!line.matches("\\w.*")) {
                    break;
                }
                final String headerPair[] = line.split(":", 2);
                if (headerPair.length == 2) {
                    final String value = response.getHeader(headerPair[0]);
                    assertEquals("Response header is not match. " + headerPair[0], headerPair[1], value);
                }
            }

            // Process Body
            final StringBuilder expectedBody = new StringBuilder();
            while (line != null) {
                expectedBody.append(line);
                line = reader.readLine();
                if (line != null) {
                    expectedBody.append('\n');
                }
            }

            final String content = response.getContentAsString();

            final String expected = expectedBody.toString().replace(' ', '.').replace('\n', '/');
            final String actual = content.replace(' ', '.').replace('\n', '/');
            assertEquals(expected, actual);
        }

        // for (String headerName: response.getHeaderNames()) {
        // System.out.println(headerName + ":" + response.getHeader(headerName));
        // }
    }

    /**
     * 第一引数に指定したファイルパスを処理した結果を第二引数の内容と比較する。 
     * 指定するパスはクラスパスルート( src/test/resources)をルートとする。
     * 
     * @param targetContentPath
     * @param expectedContentPath
     * @param pageScopeAttribute
     */
    protected void execAndVerify(final String targetContentPath, final String expectedContentPath,
            final Map<String, Object> pageScopeAttribute) throws IOException {
        final MockHttpServletRequest request = createRequest(targetContentPath);

        // When
        final MockHttpServletResponse response = exec(request, pageScopeAttribute);

        // Then
        verifyResponse(response, expectedContentPath);
    }

}

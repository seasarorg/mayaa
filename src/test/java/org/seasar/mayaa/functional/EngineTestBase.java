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
import java.util.Iterator;
import java.util.Map;
import java.util.logging.LogManager;

import org.junit.Before;
import org.junit.BeforeClass;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.engine.ProcessorDump;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.source.SourceHolderFactory;
import org.seasar.mayaa.impl.util.StringUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class EngineTestBase {

    private MockServletContext servletContext;
    private Engine engine;
    private String requestedPageName;

    @BeforeClass
    public static void init() throws SecurityException, IOException {
        try (InputStream in = EngineTestBase.class.getClassLoader().getResourceAsStream("jul.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        }

        SourceHolderFactory.release();
        FactoryFactory.release();
        FactoryFactory.setInstance(new FactoryFactoryImpl());
    }

    public void enableDump() {
        engine.setParameter(EngineImpl.DUMP_ENABLED, "true");
    }

    public void disableDump() {
        engine.setParameter(EngineImpl.DUMP_ENABLED, "false");
    }

    public Page getPage() {
        return engine.getPage(requestedPageName);
    }

    /**
     * Mavenかで実行されているかどうかを判定する。 pom.xml内でmaven-surefire-plugin経由で
     * システムプロパティを設定している。
     * 
     * @return　Mavenのテストフェーズから実行されているときにtrue
     */
    boolean isRunUnderMaven() {
        if (System.getProperty("inMaven") != null) {
            return true;
        }
        return false;
    }

    public void printProcessorTree() {
        if (!isRunUnderMaven()) {
            return;
        }

        ProcessorDump dump = new ProcessorDump();
        dump.setPrintContents(false);
        dump.setIndentChar("~~");

        Page page = getPage();
        dump.printSource(page);
    }


    public void printPageTree() {
        if (!isRunUnderMaven()) {
            return;
        }

        Page page = getPage();
        System.out.println("PAGE DUMP ==============");
        printTree(page);
    }

    public void printTemplateTree() {
        if (!isRunUnderMaven()) {
            return;
        }

        Page page = getPage();
        Template template = page.getTemplate(null, "html");
        System.out.println("TEMPLATE DUMP ==============");
        printTree(template);
    }

    public void printTree() {
        printPageTree();
        printTemplateTree();
        printProcessorTree();
    }

    public void printTree(NodeTreeWalker node) {
        System.out.println(node.getSystemID());
        printTree(node, 0);
    }

    String toPrefixAwareString(SpecificationNode sn) {
        QName qn = sn.getQName();

        if (CONST_IMPL.URI_MAYAA.equals(qn.getNamespaceURI())) {
            return "m:" + qn.getLocalName();
        }
        else if (CONST_IMPL.URI_HTML.equals(qn.getNamespaceURI())) {
            return "html4:" + qn.getLocalName();
        }
        else if (!StringUtil.isEmpty(sn.getPrefix())) {
            return sn.getPrefix() + ":" + qn.getLocalName();
        }
        else {
            return qn.toString();
        }
    }

    String toPrefixAwareString(NodeAttribute attr) {
        QName qn = attr.getQName();

        if (CONST_IMPL.URI_MAYAA.equals(qn.getNamespaceURI())) {
            return "m:" + qn.getLocalName();
        }
        else if (CONST_IMPL.URI_HTML.equals(qn.getNamespaceURI())) {
            return "html4:" + qn.getLocalName();
        }
        else if (!StringUtil.isEmpty(attr.getPrefix())) {
            return attr.getPrefix() + ":" + qn.getLocalName();
        }
        else {
            return qn.toString();
        }
    }

    public void printTree(NodeTreeWalker node, int indent) {
        String padding = "                    ";

        for (Iterator<NodeTreeWalker> itr = node.iterateChildNode(); itr.hasNext();) {
            NodeTreeWalker n = itr.next();

            SpecificationNode sn = (SpecificationNode) n;

            System.out.print(padding.substring(0, indent * 2) + "/");
            System.out.print(toPrefixAwareString(sn));
            if (sn.iterateAttribute().hasNext()) {
                for (Iterator<NodeAttribute> nodeItr = sn.iterateAttribute(); nodeItr.hasNext();){
                    NodeAttribute attr = nodeItr.next();
                    System.out.print(" " + toPrefixAwareString(attr) + "='" + attr.getValue() + "'");
                }
                System.out.println();
            }
            else {
                System.out.println();
            }


            if (n.getChildNodeSize() > 0) {
                printTree(n, indent + 1);
            }
        }
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
        // デフォルトのエラーハンドラを無効化して内部の例外でJUnitを失敗させる。
        engine.setErrorHandler(null);

        disableDump();
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

        requestedPageName = request.getServletPath();
        if (requestedPageName.lastIndexOf(".") != -1) {
            requestedPageName = requestedPageName.substring(0, requestedPageName.lastIndexOf("."));
        }
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
        verifyResponse(response, expectedContentPath, null);
    }

    protected void verifyResponse(final MockHttpServletResponse response, final String expectedContentPath, String message) throws IOException {

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
            final String content = response.getContentAsString();
            int lineIndex = 1;
            for (String actualLine: content.split("\n")) {
                String expectedLine = line;
                assertEquals("body compare:" + lineIndex, expectedLine, actualLine); 

                lineIndex++;
                line = reader.readLine();
            }
        }
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

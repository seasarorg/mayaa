package org.seasar.mayaa.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;
import java.util.logging.LogManager;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

public class EngineTestBase {

    MockServletContext servletContext;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    private Engine engine;
    
    @BeforeEach
    public void init() throws SecurityException, IOException {
        // System.setProperty("java.util.logging.config.file", "logging.properties");
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("jul.properties")) {

            // if (in == null) {
            //     System.err.println("logging.propertiesファイルがクラスパスに存在しません。");
            // }
            LogManager.getLogManager().readConfiguration(in);
        }
        
        // @Mockアノテーションのモックオブジェクトを初期化
        MockitoAnnotations.initMocks(this);
        
        servletContext = spy(new MockServletContext());
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(servletContext);
        
        engine = ProviderUtil.getEngine();
        // engine.setParameter(EngineImpl.DUMP_ENABLED, "true");
        // engine.setParameter(EngineImpl.PAGE_SERIALIZE, "true");
    }

    /**
     * 指定したHTMLファイルに対してEngineの本体処理を呼び出す
     * @param path 処理するHTMLファイルへのパス（クラスパスルートからのパス）
     * @param pageScopeAttribute あらかじめページスコープに定義済のものとして引き渡す属性のマップ
     */
    void exec(final String path, final Map<String, Object> pageScopeAttribute) {
        request.setServletPath(path);
        
        CycleUtil.initialize(request, response);
        engine.doService(pageScopeAttribute, true);
        
        verify(servletContext).getRealPath(path);
    }
    
    /**
     * Engineの処理結果として期待する内容に合致しているかを検証する。
     * @param expectedContentPath 機体結果の内容が保管されているファイルへのパス（クラスパスルートからのパス）
     * @throws IOException IOエラーが発生した場合
     */
    void verifyResponse(final String expectedContentPath) throws IOException {

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
                    assertEquals(headerPair[1], value, "Response header is not match. " + headerPair[0]);
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

}

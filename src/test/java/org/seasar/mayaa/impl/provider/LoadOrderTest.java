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
package org.seasar.mayaa.impl.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.springframework.mock.web.MockServletContext;

/**
 * Forward/Backwardの読み込み順序による動作の違いをテストする。
 * 
 * @author Test
 */
public class LoadOrderTest {

    private MockServletContext mockServletContext;

    @BeforeEach
    public void setUp() {
        // テスト用のWEB-INFディレクトリを参照するMockServletContextを設定
        String workingDir = System.getProperty("user.dir");
        String srcPath = workingDir + "/src/test/java/org/seasar/mayaa/impl/provider";
        mockServletContext = new MockServletContext() {
            @Override
            public String getRealPath(String path) {
                if ("/WEB-INF".equals(path) || "WEB-INF".equals(path)) {
                    return srcPath + "/WEB-INF";
                }
                return super.getRealPath(path);
            }
            
            @Override
            public java.net.URL getResource(String path) throws java.net.MalformedURLException {
                // WEB-INF配下のリソースは対応できるようにする
                if (path != null && path.startsWith("/WEB-INF")) {
                    String filePath = srcPath + "/WEB-INF" + path.substring("/WEB-INF".length());
                    java.io.File file = new java.io.File(filePath);
                    if (file.exists()) {
                        return file.toURI().toURL();
                    }
                }
                return super.getResource(path);
            }
        };
        FactoryFactory.reset();
        // デフォルト状態: Forward順序（Backwardは disabled）
        FactoryFactory.setEnableBackwardOrderLoadingOverride(null);
    }

    @AfterEach
    public void tearDown() {
        // クリーンアップ
        FactoryFactory.setEnableBackwardOrderLoadingOverride(null);
    }

    /**
     * Forward順序がデフォルト（Backwardが disabled）であることを確認。
     */
    @Test
    public void testDefaultIsForwardOrder() {
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Default should be Forward order (Backward disabled)");
    }

    /**
     * Backward順序を有効にできることを確認。
     */
    @Test
    public void testEnableBackwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.TRUE);
        assertFalse(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Backward order should be enabled when override=TRUE");
    }

    /**
     * Forward順序を明示的に指定できることを確認。
     */
    @Test
    public void testExplicitlyDisableBackwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.FALSE);
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Backward order should be disabled when override=FALSE");
    }

    /**
     * Overrideを null に設定するとデフォルトに戻ることを確認。
     */
    @Test
    public void testResetOverrideToDefault() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.TRUE);
        assertFalse(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Backward should be enabled");
        
        FactoryFactory.setEnableBackwardOrderLoadingOverride(null);
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading(),
                "After reset, should return to default (Forward order)");
    }

    /**
     * Forward順序でのFactoryFactory初期化を確認。
     * Built-in → META-INF → WEB-INF の順で探索し、
     * 全てを読み込みながら後のものが前のものを上書き。
     */
    @Test
    public void testFactoryFactoryForwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.FALSE);
        
        // FactoryFactoryImpl を初期化
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(mockServletContext);
        
        // Forward順序で読み込まれることを確認
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Forward order should be active");
        
        // CycleFactory が取得でき、正しく初期化されていることを確認
        assertNotNull(FactoryFactory.getCycleFactory(),
                "CycleFactory should be loaded");
        
        // Forward順序では WEB-INF の設定が最後に読み込まれ Built-in を上書きする
        assertEquals("org.seasar.mayaa.impl.cycle.web.MockServiceCycleForLoadOrderTest",
                FactoryFactory.getCycleFactory().getServiceClass().getName(),
                "Forward order: WEB-INF config should override Built-in (last wins)");
    }

    /**
     * Backward順序でのFactoryFactory初期化を確認。
     * WEB-INF → META-INF → Built-in の順で探索し、
     * 最初に見つかった有効なものを返す。
     */
    @Test
    public void testFactoryFactoryBackwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.TRUE);
        
        // FactoryFactoryImpl を初期化
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(mockServletContext);
        
        // Backward順序で読み込まれることを確認
        assertFalse(FactoryFactory.isDisabledBackwardOrderLoading(),
                "Backward order should be active");
        
        // CycleFactory が取得でき、正しく初期化されていることを確認
        assertNotNull(FactoryFactory.getCycleFactory(),
                "CycleFactory should be loaded");
        
        // Backward順序では WEB-INF の設定が最初に有効と判定され、それを返す
        assertEquals("org.seasar.mayaa.impl.cycle.web.MockServiceCycleForLoadOrderTest",
                FactoryFactory.getCycleFactory().getServiceClass().getName(),
                "Backward order: WEB-INF config should be used (first valid wins)");
    }

    /**
     * ServiceProvider の読み込みが Forward順序で動作することを確認。
     * Engine, LibraryManager など必須コンポーネントが正しく読み込まれることを検証。
     */
    @Test
    public void testServiceProviderLoadingWithForwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.FALSE);
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(mockServletContext);
        
        // Forward順序での ProviderFactory 取得
        assertNotNull(FactoryFactory.getProviderFactory(),
                "ProviderFactory should be accessible");
        
        // Engine が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getEngine(),
                "Engine should be loaded in ServiceProvider");
        
        // LibraryManager が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getLibraryManager(),
                "LibraryManager should be loaded in ServiceProvider");
        
        // TemplateBuilder が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getTemplateBuilder(),
                "TemplateBuilder should be loaded in ServiceProvider");
    }

    /**
     * ServiceProvider の読み込みが Backward順序で動作することを確認。
     * Engine, LibraryManager など必須コンポーネントが正しく読み込まれることを検証。
     */
    @Test
    public void testServiceProviderLoadingWithBackwardOrder() {
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.TRUE);
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(mockServletContext);
        
        // Backward順序での ProviderFactory 取得
        assertNotNull(FactoryFactory.getProviderFactory(),
                "ProviderFactory should be accessible");
        
        // Engine が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getEngine(),
                "Engine should be loaded in ServiceProvider");
        
        // LibraryManager が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getLibraryManager(),
                "LibraryManager should be loaded in ServiceProvider");
        
        // TemplateBuilder が読み込まれていることを確認
        assertNotNull(FactoryFactory.getProviderFactory().getServiceProvider().getTemplateBuilder(),
                "TemplateBuilder should be loaded in ServiceProvider");
    }

    /**
     * isDisabledBackwardOrderLoading() の状態遷移を検証。
     */
    @Test
    public void testBackwardOrderLoadingStateTransitions() {
        // 初期状態: Forward（Backward disabled = true）
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading());
        
        // Backward有効化
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.TRUE);
        assertFalse(FactoryFactory.isDisabledBackwardOrderLoading());
        
        // Forward明示指定
        FactoryFactory.setEnableBackwardOrderLoadingOverride(Boolean.FALSE);
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading());
        
        // リセット
        FactoryFactory.setEnableBackwardOrderLoadingOverride(null);
        assertTrue(FactoryFactory.isDisabledBackwardOrderLoading(),
                "After reset, should default to Forward");
    }
}

package org.seasar.mayaa.impl.cycle.script.rhino;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.provider.ProviderFactory;

import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.scope.StubApplicationScopeSupport;

public class ScriptEnvironmentTest {

    @BeforeClass
    public static void init() {
        FactoryFactory.release();
    }

    @Test
    public void testキャッシュサイズの指定() {
        //-- Given
        // * ./WEB-INF/org.seasar.mayaa.provider.ServiceProvider 内の
        //   scriptEnvironment cacheSize に 256 が設定されている
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(new StubApplicationScopeSupport(this));

        //-- When
        // ファクトリーを経由してScriptEnvironmentインスタンスを生成する
        ProviderFactory providerFactory = (ProviderFactory) FactoryFactory.getFactory(ProviderFactory.class);
        // ServiceProviderのインスタンスを取得することでScriptEnvironmentを作成する。
        providerFactory.getServiceProvider();

        //-- Then
        // スクリプトのキャッシュ設定値が256に設定されている。
        assertEquals(256, ScriptEnvironmentImpl.getScriptCacheSize());
    }
}

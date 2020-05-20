package org.seasar.mayaa.impl.cycle.script.rhino;

import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.provider.ProviderFactory;

import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.scope.StubApplicationScopeSupport;

import junit.framework.TestCase;

public class ScriptEnvironmentTest extends TestCase {

    public void testキャッシュサイズの指定() {
        //-- Given
        // ./WEB-INF/org.seasar.mayaa.provider.ServiceProvider 内の
        // scriptEnvironment cacheSize に 256 が設定されている

        //-- When
        // ファクトリーを経由してScriptEnvironmentインスタンスを生成する
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(new StubApplicationScopeSupport(this));
        ProviderFactory providerFactory = (ProviderFactory) FactoryFactory.getFactory(ProviderFactory.class);
        // ServiceProviderのインスタンスを取得することでScriptEnvironmentを作成する。
        providerFactory.getServiceProvider();

        //-- Then
        // スクリプトのキャッシュ設定値が256に設定されている。
        assertEquals(256, ScriptEnvironmentImpl.getScriptCacheSize());
    }
}

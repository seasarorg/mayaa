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
        ScriptEnvironmentImpl scriptEnvironmentImpl = (ScriptEnvironmentImpl) providerFactory.getServiceProvider()
                .getScriptEnvironment();

        //-- Then
        // スクリプトのキャッシュ設定値が256に設定されている。
        assertEquals(256, scriptEnvironmentImpl.getScriptCacheSize());
    }
}

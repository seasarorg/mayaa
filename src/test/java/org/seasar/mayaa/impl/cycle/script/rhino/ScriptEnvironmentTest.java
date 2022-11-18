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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.ComplexScript;
import org.seasar.mayaa.impl.provider.ProviderUtil;

public class ScriptEnvironmentTest {

    ScriptEnvironmentImpl testee;

    @BeforeAll
    public static void init() {
        FactoryFactory.release();
        FactoryFactory.setInstance(new FactoryFactoryImpl());
    }

    @BeforeEach
    public void setup() {
        // -- Given
        // * ./WEB-INF/org.seasar.mayaa.provider.ServiceProvider 内の
        // scriptEnvironment cacheSize に 256 が設定されている
        // FactoryFactory.setContext(new StubApplicationScopeSupport(this));
        FactoryFactory.setContext(new MockServletContext());

        // -- When
        // ファクトリーを経由してScriptEnvironmentインスタンスを生成する
        // ServiceProviderのインスタンスを取得することでScriptEnvironmentを作成する。
        testee = (ScriptEnvironmentImpl) ProviderUtil.getScriptEnvironment();

    }

    @Test
    public void testキャッシュサイズの指定() {
        // setupメソッドで実行することをいったんリセットして本メソッド内で実行
        init();

        // -- Given
        // * ./WEB-INF/org.seasar.mayaa.provider.ServiceProvider 内の
        // scriptEnvironment cacheSize に 256 が設定されている
        FactoryFactory.setContext(new MockServletContext(this.getClass().getPackage().getName().replace('.', '/')));

        // -- When
        // ファクトリーを経由してScriptEnvironmentインスタンスを生成する
        // ServiceProviderのインスタンスを取得することでScriptEnvironmentを作成する。
        testee = (ScriptEnvironmentImpl) ProviderUtil.getScriptEnvironment();

        // -- Then
        // スクリプトのキャッシュ設定値が256に設定されている。
        assertEquals(256, testee.getScriptCacheSize());
    }

    CompiledScript runScript(String script) {
        final PositionAware position = new Position("/1", 1);

        CompiledScript compiledScript = testee.compile(script, position);
        // System.out.println(compiledScript);


        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPathInfo("/path/to/page.html");
        CycleUtil.initialize(request, new MockHttpServletResponse());

        testee.startScope(null);
        compiledScript.execute(null);
        // System.out.println(result);
        testee.endScope();

        return compiledScript;
    }

    @Test
    public void コンパイルを行う_複数ブロック() {
        final String script = "ABC    ${  var a = 0; a += 10 * 2 }  XYZ";
        CompiledScript compiledScript = runScript(script);
        assertThat(compiledScript, instanceOf(ComplexScript.class));
    }


    @Test
    public void コンパイルを行う_関数定義() {
        final String script = "${		function println(value) {"
        + "                'run script' + value;"
        + "            }"
        + "            println('hello')}";

        CompiledScript compiledScript = runScript(script);
        assertThat(compiledScript, instanceOf(TextCompiledScriptImpl.class));
    }

    @Test
    public void コンパイルを行う_Javaクラス参照() {
        final String script = "${java.lang.Math.PI}";

        CompiledScript compiledScript = runScript(script);
        assertThat(compiledScript, instanceOf(TextCompiledScriptImpl.class));
    }

    @Test
    public void コンパイルを行う_システムプロパティ参照() {
        final String script = "Java Runtime: ${java.lang.System.getProperty('test.value')}";

        System.setProperty("test.value", "TEST VALUE");
        CompiledScript compiledScript = runScript(script);
        assertThat(compiledScript, instanceOf(ComplexScript.class));
    }

    @Test
    public void 複数スレッドでコンパイルを行う() throws InterruptedException {
        final String[] scripts = {
            "ABC    ${  var a = 0; a += 10 * 2 }  XYZ",
            "${		function println(value) {"
            + "                java.lang.System.out.println(java.lang.String.valueOf(value));"
            + "            }"
            + "            println('run script')}",
            "Java Runtime: ${java.lang.System.getProperty('java.version')}" 
        };

        final PositionAware position = new Position("/1", 1);

        testee.setScriptCacheSize(1);

        final int threadsCount = 100;
        final Random random = new Random();
        final CountDownLatch latch = new CountDownLatch(threadsCount);

        Thread[] threads = new Thread[threadsCount];
        for (int requestId = 0; requestId < threadsCount; requestId++) {
            final int scriptIndex = random.nextInt(scripts.length);

            threads[requestId] = new Thread() {
                public void run() {
                    try {
                        // 一斉に動いた方がぶつかりやすいので待ち合わせる
                        latch.countDown();
                        latch.await();

                        CompiledScript compiledScript = testee.compile(scripts[scriptIndex], position);
                        assertEquals(scripts[scriptIndex], compiledScript.getScriptText());

                        MockHttpServletRequest request = new MockHttpServletRequest();
                        request.setPathInfo("/path/to/page.html");
                        CycleUtil.initialize(request, new MockHttpServletResponse());
                
                        testee.startScope(null);
                        compiledScript.execute(null);
                        testee.endScope();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            threads[requestId].start();
        }
        // スレッドの終了を待ち合わせる
        for (int requestId = 0; requestId < threadsCount; requestId++) {
            threads[requestId].join();
        }

        System.out.println("End");
    }
}

class Position implements PositionAware {

    String systemId;
    int lineNumber;

    public Position(String systemId, int lineNumber) {
        this.systemId = systemId;
        this.lineNumber = lineNumber;
    }
    
    @Override
    public void setSystemID(String systemID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOnTemplate(boolean onTemplate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLineNumber(int lineNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isOnTemplate() {
        return false;
    }

    @Override
    public String getSystemID() {
        return "/ScriptEnvironmentTest";
    }

    @Override
    public int getLineNumber() {
        return 10;
    }
};

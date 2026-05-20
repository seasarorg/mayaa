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
package org.seasar.mayaa.functional.engine;

import java.io.IOException;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.engine.EngineImpl;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * テンプレートビルド（injection フェーズ）の性能測定。
 * キャッシュ最適化の効果を確認するための簡易ベンチマーク。
 * <p>
 * 通常の CI では実行されない。{@code mvn test -P withPerformanceTest} で実行する。
 */
@Tag("test.PerformanceTest")
public class BuildPerformanceTest extends EngineTestBase {

    private static final int WARMUP = 100;
    private static final int ITERATIONS = 2000;

    /**
     * XPath ベースの injection (XPathMatchesInjectionResolver) の繰り返しビルド時間を計測。
     */
    @Test
    public void bench_inject_xpath() throws IOException {
        final String TARGET = "/it-case/engine/inject_xpath/target.html";
        runBench("inject_xpath (XPath cache)", TARGET);
    }

    /**
     * ID ベースの injection (EqualsIDInjectionResolver) の繰り返しビルド時間を計測。
     */
    @Test
    public void bench_inject_mayaa() throws IOException {
        final String TARGET = "/it-case/engine/inject_mayaa/target.html";
        runBench("inject_mayaa  (ID cache)  ", TARGET);
    }

    /**
     * XPath ベースの injection — テンプレートのみ deprecate（.mayaa は再読み込みしない）。
     * injection フェーズ単体の性能を計測しやすい。
     */
    @Test
    public void bench_inject_xpath_templateonly() throws IOException {
        final String TARGET = "/it-case/engine/inject_xpath/target.html";
        runBench("inject_xpath (template-only) ", TARGET, false);
    }

    /**
     * ID ベースの injection — テンプレートのみ deprecate（.mayaa は再読み込みしない）。
     */
    @Test
    public void bench_inject_mayaa_templateonly() throws IOException {
        final String TARGET = "/it-case/engine/inject_mayaa/target.html";
        runBench("inject_mayaa  (template-only)", TARGET, false);
    }

    // ------------------------------------------------------------------

    private void runBench(String label, String target) throws IOException {
        runBench(label, target, true, false);
    }

    private void runBench(String label, String target, boolean deprecateMayaa) throws IOException {
        runBench(label, target, deprecateMayaa, false);
    }

    private void runBench(String label, String target, boolean deprecateMayaa, boolean deprecateDefault) throws IOException {
        EngineImpl engine = (EngineImpl) ProviderUtil.getEngine();

        // ウォームアップ（JIT 最適化を安定させる）
        for (int i = 0; i < WARMUP; i++) {
            execAndDeprecate(engine, target, deprecateMayaa, deprecateDefault);
        }

        // 計測
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            execAndDeprecate(engine, target, deprecateMayaa, deprecateDefault);
        }
        long elapsed = System.nanoTime() - start;

        double totalMs = elapsed / 1_000_000.0;
        double perMs   = totalMs / ITERATIONS;
        System.out.printf("[BENCH] %-38s  %d iters  total=%.1f ms  per=%.3f ms%n",
                label, ITERATIONS, totalMs, perMs);
    }

    private void execAndDeprecate(EngineImpl engine, String path, boolean deprecateMayaa, boolean deprecateDefault) {
        MockHttpServletRequest request = createRequest(path);
        exec(request, null);
        // キャッシュを無効化して毎回ビルドを走らせる
        engine.deprecateSpecification(path);
        if (deprecateMayaa) {
            // .mayaa も無効化する（injection ルールの再読み込みを強制）
            String mayaaPath = path.replaceAll("\\.[^.]+$", ".mayaa");
            engine.deprecateSpecification(mayaaPath);
        }
        if (deprecateDefault) {
            // default.mayaa も無効化する（全スペック再構築を強制）
            engine.deprecateSpecification("/default.mayaa");
        }
    }
}

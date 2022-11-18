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
package org.seasar.mayaa.regressions.issue14;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.util.WeakValueHashMap;

/**
 * https://github.com/seasarorg/mayaa/issues/14 を元に {@code WeakValueHashMap} で {@link NoSuchElementException} が
 * 発生する問題の再現を確認するコードとしてJavaに焼き直したもの。
 * 再現するとJUnitとして失敗する。
 */
public class Issue14ReproductionTest {

    static final int cacheSize = 3; // Mayaaの実装と同じ

    static final AtomicBoolean foundNSEE = new AtomicBoolean(false);
    static final WeakValueHashMap<String, String> l = new WeakValueHashMap<>(cacheSize);

    public static void main(final String[] args) throws InterruptedException {
        final Issue14ReproductionTest testee = new Issue14ReproductionTest();
        testee.testReproduceTrial();
    }

    /**
     * repdocudeMainを何回か試行して再現確認を行う。
     */
    @Test
    public void testReproduceTrial() throws InterruptedException {
        final int THREAD_COUNT = 20;
        final int TRIAL_COUNT = 1000;

        for (int trialId = 0; trialId < TRIAL_COUNT && !foundNSEE.get(); trialId++) {
            if (reproduceMain(trialId, THREAD_COUNT)) {
                fail("Reproduced! Trial:" + trialId);
            }
        }
    }


    /**
     * NoSuchElementExceptioの再現の主処理。
     * 複数のスレッドで同時に WeakValueHashMapを参照する。
     * 
     * @param trialId 試行番号
     * @param threadsCount 同時実行するスレッド数
     * @return NoSuchElementExceptionが発生したら(再現したら) true
     * @throws InterruptedException スレッド待ち合わせ処理が中断した場合
     */
    boolean reproduceMain(final int trialId, final int threadsCount) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(threadsCount);

        Random random = new Random(trialId);

        Thread[] threads = new Thread[threadsCount];
        for (int requestId = 0; requestId < threadsCount; requestId++) {
            final String id = "" + random.nextInt(10);
            if (random.nextInt(100) == 0) {
                System.gc();
                // System.out.println("gc---------");
            }
            // if (requestId % 100 == 0) {
            //     System.out.println(l.getMaxCountOfDroppedRecord());
            // }

            threads[requestId] = new Thread() {
                public void run() {
                    try {
                        String key = "ITEM:" + id;

                        // 一斉に動いた方がぶつかりやすいので待ち合わせる
                        latch.countDown();
                        latch.await();

                        l.put(key, id);
                        l.get(key);
                    } catch (final NoSuchElementException e) {
                        e.printStackTrace(System.err);
                        foundNSEE.set(true);
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

        //　結果の判定
        return foundNSEE.get();
    }


}

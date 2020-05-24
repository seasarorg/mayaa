package org.seasar.mayaa.regressions.issue14;

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.seasar.mayaa.impl.util.WeakValueHashMap;

import junit.framework.TestCase;

/**
 * https://github.com/seasarorg/mayaa/issues/14 を元に {@code WeakValueHashMap} で {@link NoSuchElementException} が
 * 発生する問題の再現を確認するコードとしてJavaに焼き直したもの。
 * 再現するとJUnitとして失敗する。
 */
public class Issue14ReproductionTest extends TestCase {

    static final int cacheSize = 128; // Mayaaの実装と同じ

    static final AtomicBoolean foundNSEE = new AtomicBoolean(false);
    static final WeakValueHashMap l = new WeakValueHashMap(cacheSize);

    public static void main(final String[] args) throws InterruptedException {
        final Issue14ReproductionTest testee = new Issue14ReproductionTest();
        testee.testReproduceTrial();
    }

    /**
     * repdocudeMainを何回か試行して再現確認を行う。
     */
    public void testReproduceTrial() throws InterruptedException {
        final int THREAD_COUNT = 10;
        final int TRIAL_COUNT = 200;

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

        Thread[] threads = new Thread[threadsCount];
        for (int requestId = 0; requestId < threadsCount; requestId++) {
            final String id = "[" + trialId + "]" + requestId;

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

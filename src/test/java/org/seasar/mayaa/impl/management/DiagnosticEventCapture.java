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
package org.seasar.mayaa.impl.management;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

/**
 * テスト用 DiagnosticEventBuffer イベントキャプチャー。
 * DiagnosticEventBuffer にリスナーとして登録し、発行されたイベントを
 * インスタンス内のバッファに蓄積する。
 *
 * <p>使用例：
 * <pre>
 *   DiagnosticEventCapture capture = new DiagnosticEventCapture();
 *
 *   \@BeforeEach void setUp() { capture.start(); }
 *   \@AfterEach  void tearDown() { capture.stop(); }
 *
 *   void test() {
 *       // ... engine処理 ...
 *       List&lt;DiagnosticEventBuffer.Event&gt; events = capture.snapshot();
 *   }
 * </pre>
 * </p>
 */
public class DiagnosticEventCapture {

    private static final int DEFAULT_CAPACITY = 512;

    private final Deque<DiagnosticEventBuffer.Event> _events =
            new ArrayDeque<>(DEFAULT_CAPACITY);

    private final Consumer<DiagnosticEventBuffer.Event> _listener = event -> {
        synchronized (DiagnosticEventCapture.this) {
            while (_events.size() >= DEFAULT_CAPACITY) {
                _events.removeFirst();
            }
            _events.addLast(event);
        }
    };

    /** DiagnosticEventBuffer にリスナーとして登録し、キャプチャーを開始する。 */
    public void start() {
        DiagnosticEventBuffer.addListener(_listener);
    }

    /** DiagnosticEventBuffer からリスナーを解除し、キャプチャーを停止する。 */
    public void stop() {
        DiagnosticEventBuffer.removeListener(_listener);
    }

    /** キャプチャーしたイベントのスナップショットを（古い順で）返す。 */
    public synchronized List<DiagnosticEventBuffer.Event> snapshot() {
        return new ArrayList<>(_events);
    }

    /**
     * 指定タイムスタンプより後のイベントのみ返す（差分ポーリング用）。
     *
     * @param sinceMillis この値より {@code timestampMillis} が大きいイベントのみ返す。
     *                    0 を指定するとバッファ内の全件を返す。
     */
    public synchronized List<DiagnosticEventBuffer.Event> snapshotSince(long sinceMillis) {
        List<DiagnosticEventBuffer.Event> result = new ArrayList<>();
        for (DiagnosticEventBuffer.Event e : _events) {
            if (e.timestampMillis() > sinceMillis) {
                result.add(e);
            }
        }
        return result;
    }

    /** キャプチャー済みイベントをすべて破棄する。 */
    public synchronized void clear() {
        _events.clear();
    }

    /** キャプチャー済みイベント数を返す。 */
    public synchronized int size() {
        return _events.size();
    }
}

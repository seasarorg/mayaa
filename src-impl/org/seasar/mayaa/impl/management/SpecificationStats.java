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
import java.util.concurrent.atomic.AtomicLong;

import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;

/**
 * キャッシュ上に保持されている単一の Specification (Page または Template) に対する
 * プロファイリングデータを保持するクラス。
 *
 * <p>複数スレッドから同時アクセスされることを想定し、各カウンタには
 * {@link AtomicLong} を使用している。</p>
 *
 * @since 2.0
 * @author Watanabe, Mitsutaka
 */
public class SpecificationStats {

    /** Specification の種別: "page" または "template" */
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_TEMPLATE = "template";

    /**
     * DiagnosticEventBuffer.Phase の ordinal 値 (フェーズ別診断カウンタのインデックス)。
     * DiagnosticEventBuffer への直接依存を避けるため定数として保持する。
     */
    public static final int PHASE_PARSE  = 0;
    public static final int PHASE_BUILD  = 1;
    public static final int PHASE_RENDER = 2;
    public static final int PHASE_COUNT  = 3;

    private final String systemID;
    private final String type;

    // ---- ビルド統計 ----
    private final AtomicLong buildCount      = new AtomicLong(0);
    private final AtomicLong totalBuildTimeMs = new AtomicLong(0);
    private final AtomicLong maxBuildTimeMs   = new AtomicLong(0);
    private volatile long lastBuildTimeMs    = 0L;
    private volatile long lastBuiltAt        = 0L;  // epoch ms
    private final AtomicLong buildErrorCount = new AtomicLong(0);

    // ---- レンダリング統計 ----
    private final AtomicLong renderCount      = new AtomicLong(0);
    private final AtomicLong totalRenderTimeMs = new AtomicLong(0);
    private final AtomicLong maxRenderTimeMs   = new AtomicLong(0);
    private volatile long lastRenderTimeMs   = 0L;
    private volatile long lastRenderedAt     = 0L;  // epoch ms
    private final AtomicLong renderErrorCount = new AtomicLong(0);

    // ---- 診断イベントカウンタ + per-spec イベントバッファ ----
    // インデックス: PHASE_PARSE=0, PHASE_BUILD=1, PHASE_RENDER=2
    private static final int EVENT_BUFFER_CAPACITY = 100;
    private final AtomicLong[] _diagWarnCounts  = {
            new AtomicLong(), new AtomicLong(), new AtomicLong() };
    private final AtomicLong[] _diagErrorCounts = {
            new AtomicLong(), new AtomicLong(), new AtomicLong() };
    private final Deque<DiagnosticEventBuffer.Event> _events =
            new ArrayDeque<>(EVENT_BUFFER_CAPACITY);

    public SpecificationStats(String systemID, String type) {
        if (systemID == null || type == null) {
            throw new IllegalArgumentException();
        }
        this.systemID = systemID;
        this.type     = type;
    }

    // ----------------------------------------------------------------
    // 記録メソッド
    // ----------------------------------------------------------------

    /**
     * ビルド（パース）完了時に呼び出す。
     * エラーの場合も呼び出し、別途 {@link #recordBuildError()} を呼ぶこと。
     *
     * @param durationMs ビルドにかかった時間 (ms)
     */
    public void recordBuild(long durationMs) {
        buildCount.incrementAndGet();
        totalBuildTimeMs.addAndGet(durationMs);
        updateMax(maxBuildTimeMs, durationMs);
        lastBuildTimeMs = durationMs;
        lastBuiltAt     = System.currentTimeMillis();
    }

    /** ビルド中に例外が発生した場合に呼び出す。 */
    public void recordBuildError() {
        buildErrorCount.incrementAndGet();
    }

    /**
     * レンダリング完了時に呼び出す。
     * エラーの場合も呼び出し、別途 {@link #recordRenderError()} を呼ぶこと。
     *
     * @param durationMs レンダリングにかかった時間 (ms)
     */
    public void recordRender(long durationMs) {
        renderCount.incrementAndGet();
        totalRenderTimeMs.addAndGet(durationMs);
        updateMax(maxRenderTimeMs, durationMs);
        lastRenderTimeMs = durationMs;
        lastRenderedAt   = System.currentTimeMillis();
    }

    /** レンダリング中に例外が発生した場合に呼び出す。 */
    public void recordRenderError() {
        renderErrorCount.incrementAndGet();
    }

    /**
     * DiagnosticEventBuffer からイベントが発行されたときに呼び出す。
     * カウンタのインクリメントと per-spec バッファへのイベント格納を行う。
     */
    public synchronized void recordDiagEventFull(DiagnosticEventBuffer.Event event) {
        int phaseOrdinal = event.phase().ordinal();
        if (phaseOrdinal >= 0 && phaseOrdinal < PHASE_COUNT) {
            if (event.level() == DiagnosticEventBuffer.Level.ERROR) {
                _diagErrorCounts[phaseOrdinal].incrementAndGet();
            } else {
                _diagWarnCounts[phaseOrdinal].incrementAndGet();
            }
        }
        while (_events.size() >= EVENT_BUFFER_CAPACITY) {
            _events.removeFirst();
        }
        _events.addLast(event);
    }

    /** このスペックに紐づく全イベントのスナップショットを返す。 */
    public synchronized List<DiagnosticEventBuffer.Event> snapshotEvents() {
        return new ArrayList<>(_events);
    }

    /**
     * 指定タイムスタンプより後のイベントのみ返す (差分ポーリング用)。
     *
     * @param sinceMillis この値より {@code timestampMillis} が大きいイベントのみ返す。
     */
    public synchronized List<DiagnosticEventBuffer.Event> snapshotEventsSince(long sinceMillis) {
        List<DiagnosticEventBuffer.Event> result = new ArrayList<>();
        for (DiagnosticEventBuffer.Event e : _events) {
            if (e.timestampMillis() > sinceMillis) {
                result.add(e);
            }
        }
        return result;
    }

    /** per-spec イベントバッファをクリアする。 */
    public synchronized void clearEvents() {
        _events.clear();
    }

    // ----------------------------------------------------------------
    // 参照メソッド
    // ----------------------------------------------------------------

    public String getSystemID()  { return systemID; }
    public String getType()      { return type; }

    public long getBuildCount()       { return buildCount.get(); }
    public long getTotalBuildTimeMs() { return totalBuildTimeMs.get(); }
    public long getMaxBuildTimeMs()   { return maxBuildTimeMs.get(); }
    public long getLastBuildTimeMs()  { return lastBuildTimeMs; }
    public long getLastBuiltAt()      { return lastBuiltAt; }
    public long getBuildErrorCount()  { return buildErrorCount.get(); }

    public double getAvgBuildTimeMs() {
        long count = buildCount.get();
        return count == 0L ? 0.0 : (double) totalBuildTimeMs.get() / count;
    }

    public long getRenderCount()       { return renderCount.get(); }
    public long getTotalRenderTimeMs() { return totalRenderTimeMs.get(); }
    public long getMaxRenderTimeMs()   { return maxRenderTimeMs.get(); }
    public long getLastRenderTimeMs()  { return lastRenderTimeMs; }
    public long getLastRenderedAt()    { return lastRenderedAt; }
    public long getRenderErrorCount()  { return renderErrorCount.get(); }

    public double getAvgRenderTimeMs() {
        long count = renderCount.get();
        return count == 0L ? 0.0 : (double) totalRenderTimeMs.get() / count;
    }

    public long getDiagWarnCount(int phaseOrdinal)  { return _diagWarnCounts[phaseOrdinal].get(); }
    public long getDiagErrorCount(int phaseOrdinal) { return _diagErrorCounts[phaseOrdinal].get(); }

    public long getTotalDiagWarnCount() {
        long sum = 0;
        for (AtomicLong c : _diagWarnCounts) sum += c.get();
        return sum;
    }

    public long getTotalDiagErrorCount() {
        long sum = 0;
        for (AtomicLong c : _diagErrorCounts) sum += c.get();
        return sum;
    }

    /** 警告または診断エラーが 1 件以上存在するか */
    public boolean hasDiagnosticIssues() {
        return getTotalDiagWarnCount() > 0 || getTotalDiagErrorCount() > 0;
    }

    /** ビルドエラー・レンダリングエラー・診断エラーのいずれかが存在するか */
    public boolean hasAnyErrors() {
        return getBuildErrorCount() > 0 || getRenderErrorCount() > 0
                || getTotalDiagErrorCount() > 0;
    }

    // ----------------------------------------------------------------
    // 管理
    // ----------------------------------------------------------------

    /** このエントリのプロファイリングデータをリセットする。 */
    public void reset() {
        buildCount.set(0);
        totalBuildTimeMs.set(0);
        maxBuildTimeMs.set(0);
        lastBuildTimeMs  = 0L;
        lastBuiltAt      = 0L;
        buildErrorCount.set(0);

        renderCount.set(0);
        totalRenderTimeMs.set(0);
        maxRenderTimeMs.set(0);
        lastRenderTimeMs = 0L;
        lastRenderedAt   = 0L;
        renderErrorCount.set(0);

        for (int i = 0; i < PHASE_COUNT; i++) {
            _diagWarnCounts[i].set(0);
            _diagErrorCounts[i].set(0);
        }
        clearEvents();
    }

    // ----------------------------------------------------------------
    // JSON 出力
    // ----------------------------------------------------------------

    /**
     * このエントリのすべてのプロファイリングデータを JSON 形式で返す。
     * 外部の JSON ライブラリ依存を避けるため手動で組み立てている。
     */
    public String toJson() {
        return "{"
            + "\"systemID\":" + quoteJson(systemID) + ","
            + "\"type\":" + quoteJson(type) + ","
            + "\"build\":{"
                + "\"count\":"       + buildCount.get()      + ","
                + "\"totalMs\":"     + totalBuildTimeMs.get() + ","
                + "\"maxMs\":"       + maxBuildTimeMs.get()   + ","
                + "\"lastMs\":"      + lastBuildTimeMs        + ","
                + "\"avgMs\":"       + String.format("%.2f", getAvgBuildTimeMs()) + ","
                + "\"errorCount\":"  + buildErrorCount.get()  + ","
                + "\"lastBuiltAt\":" + lastBuiltAt
            + "},"
            + "\"render\":{"
                + "\"count\":"           + renderCount.get()      + ","
                + "\"totalMs\":"         + totalRenderTimeMs.get() + ","
                + "\"maxMs\":"           + maxRenderTimeMs.get()   + ","
                + "\"lastMs\":"          + lastRenderTimeMs        + ","
                + "\"avgMs\":"           + String.format("%.2f", getAvgRenderTimeMs()) + ","
                + "\"errorCount\":"      + renderErrorCount.get()  + ","
                + "\"lastRenderedAt\":"  + lastRenderedAt
            + "},"
            + "\"diag\":{"
                + "\"parse\":{\"warn\":"  + _diagWarnCounts[PHASE_PARSE].get()
                            + ",\"error\":" + _diagErrorCounts[PHASE_PARSE].get() + "},"
                + "\"build\":{\"warn\":"  + _diagWarnCounts[PHASE_BUILD].get()
                            + ",\"error\":" + _diagErrorCounts[PHASE_BUILD].get() + "},"
                + "\"render\":{\"warn\":" + _diagWarnCounts[PHASE_RENDER].get()
                            + ",\"error\":" + _diagErrorCounts[PHASE_RENDER].get() + "}"
            + "}"
            + "}";
    }

    // ----------------------------------------------------------------
    // 内部ヘルパー
    // ----------------------------------------------------------------

    /**
     * AtomicLong で保持する最大値を CAS ループで更新する。
     * 厳密な整合性より低コストを優先するため、稀に最大値が更新されない場合がある
     * ことは許容している。
     */
    private static void updateMax(AtomicLong maxRef, long value) {
        long prev;
        do {
            prev = maxRef.get();
            if (value <= prev) {
                return;
            }
        } while (!maxRef.compareAndSet(prev, value));
    }

    private static String quoteJson(String s) {
        if (s == null) {
            return "null";
        }
        // JSON の最小エスケープ: バックスラッシュ、ダブルクォート、制御文字
        StringBuilder sb = new StringBuilder(s.length() + 10);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}

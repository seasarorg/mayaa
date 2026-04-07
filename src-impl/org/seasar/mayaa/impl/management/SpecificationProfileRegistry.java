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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.management.SpecificationProfileMXBean;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * キャッシュ上に保持されている Specification (Page および Template) の
 * プロファイリングデータを管理するレジストリ。
 *
 * <p>JVM スコープのシングルトンとして動作する。
 * {@link #getInstance()} 経由でアクセスする。</p>
 *
 * <p>このクラスは起動時に自動的に JMX MXBean を登録する。
 * 外部 HTTP エンドポイントからは {@link SpecificationProfileMXBean#getSummaryAsJson()}
 * 等を経由してデータにアクセスできる。</p>
 *
 * @since 2.0
 * @author Watanabe, Mitsutaka
 */
public class SpecificationProfileRegistry {

    private static final Log LOG = LogFactory.getLog(SpecificationProfileRegistry.class);

    /**
     * _statsCache の保管上限。LRU 退去の上限値として使用する。
     */
    public static final int MAX_TRACKED = 10_000;

    private static final SpecificationProfileRegistry INSTANCE =
            new SpecificationProfileRegistry();

    private final Cache<String, SpecificationStats> _statsMap =
            Caffeine.newBuilder()
                    .maximumSize(MAX_TRACKED)
                    .build();

    private final MXBeanImpl _mxBean;

    private SpecificationProfileRegistry() {
        _mxBean = new MXBeanImpl();
        try {
            ObjectName objectName = new ObjectName(
                    SpecificationProfileMXBean.JMX_OBJECT_NAME_FORMAT);
            JMXUtil.register(_mxBean, objectName);
        } catch (MalformedObjectNameException e) {
            // ObjectName が固定文字列なので実際には到達しない
            throw new IllegalStateException("Failed to register SpecificationProfile MXBean", e);
        }

        // DiagnosticEventBuffer のイベントをログに出力するリスナー
        DiagnosticEventBuffer.addListener(event -> {
            String pos = event.positionSystemID() != null
                    ? " at " + event.positionSystemID() + ":" + event.positionLineNumber()
                    : "";
            String msg = "[" + event.phase() + "/" + event.label() + "] "
                    + event.message() + pos;
            if (event.level() == DiagnosticEventBuffer.Level.ERROR) {
                LOG.error(msg);
            } else {
                LOG.warn(msg);
            }
        });

        // DiagnosticEventBuffer のイベントを受け取り、ownerSystemID (記録時点のスコープ) に
        // 紐づく SpecificationStats の per-spec バッファに格納する。
        // ownerSystemID がない場合は positionSystemID にフォールバックする。
        DiagnosticEventBuffer.addListener(event -> {
            String routeID = event.ownerSystemID();
            if (routeID == null) {
                routeID = event.positionSystemID();
            }
            if (routeID == null) return;
            SpecificationStats stats = _statsMap.getIfPresent(routeID);
            if (stats != null) {
                stats.recordDiagEventFull(event);
            }
        });
    }

    /**
     * シングルトンインスタンスを返す。
     */
    public static SpecificationProfileRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * MXBean 実装インスタンスを返す。
     * {@link org.seasar.mayaa.impl.management.servlet.MayaaProfileServlet} など
     * JMX を経由しない呼び出し元から直接データを取得するために使用する。
     */
    public SpecificationProfileMXBean getMXBean() {
        return _mxBean;
    }

    // ----------------------------------------------------------------
    // ビルドスコープ管理
    // ----------------------------------------------------------------

    /**
     * パース・ビルドフェーズの開始を {@link DiagnosticEventBuffer} のスコープに記録する。
     * 発行された診断イベントは {@link DiagnosticEventBuffer.Event#ownerSystemID()} として
     * 記録時にキャプチャされ、この spec の統計に紐づけられる。
     * 必ず対応する {@link #endBuildScope()} を finally ブロックで呼び出すこと。
     */
    public static void beginBuildScope(String systemID) {
        DiagnosticEventBuffer.beginScope(systemID);
    }

    /** パース・ビルドフェーズのスコープを終了し、DiagnosticEventBuffer のスコープをクリアする。 */
    public static void endBuildScope() {
        DiagnosticEventBuffer.endScope();
    }

    // ----------------------------------------------------------------
    // データ記録
    // ----------------------------------------------------------------

    /**
     * 指定 systemID に対応する {@link SpecificationStats} を返す。
     * 存在しない場合は新規作成して登録する。
     *
     * @param systemID Specification の systemID
     * @param type     {@link SpecificationStats#TYPE_PAGE} または
     *                 {@link SpecificationStats#TYPE_TEMPLATE}
     */
    public SpecificationStats getOrCreate(String systemID, String type) {
        return _statsMap.get(systemID, k -> new SpecificationStats(k, type));
    }

    /**
     * 指定 systemID に対応する {@link SpecificationStats} を返す。
     * 存在しない場合は {@code null} を返す。
     */
    public SpecificationStats get(String systemID) {
        return _statsMap.getIfPresent(systemID);
    }

    /** 全エントリのコレクションを返す（スナップショット）。 */
    public Collection<SpecificationStats> getAll() {
        return new ArrayList<>(_statsMap.asMap().values());
    }

    /**
     * 指定 systemID のプロファイリングエントリを明示的に割り当てから削除する。
     * 存在しない場合は何もしない。
     */
    public void remove(String systemID) {
        _statsMap.invalidate(systemID);
    }

    // ----------------------------------------------------------------
    // 管理操作
    // ----------------------------------------------------------------

    /**
     * 指定 systemID のプロファイリングデータをリセットする。
     * 存在しない場合は何もしない。
     */
    public void reset(String systemID) {
        SpecificationStats stats = _statsMap.getIfPresent(systemID);
        if (stats != null) {
            stats.reset();
        }
    }

    /** 全エントリのプロファイリングデータをリセットする。 */
    public void resetAll() {
        _statsMap.asMap().values().forEach(SpecificationStats::reset);
    }

    /**
     * 全 SpecificationStats の per-spec バッファから指定タイムスタンプより後の
     * イベントを集約して返す（古い順）。
     *
     * @param sinceMillis 0 を指定すると全件を返す
     */
    public List<DiagnosticEventBuffer.Event> snapshotEventsSince(long sinceMillis) {
        return _statsMap.asMap().values().stream()
                .flatMap(s -> s.snapshotEventsSince(sinceMillis).stream())
                .sorted(Comparator.comparingLong(DiagnosticEventBuffer.Event::timestampMillis))
                .collect(Collectors.toList());
    }

    // ================================================================
    // JMX MXBean 実装
    // ================================================================

    private class MXBeanImpl implements SpecificationProfileMXBean {

        // ---- 集計値 ----

        @Override
        public int getTrackedCount() {
            return (int) _statsMap.estimatedSize();
        }

        @Override
        public long getAggregateBuildCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getBuildCount).sum();
        }

        @Override
        public long getAggregateTotalBuildTimeMs() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getTotalBuildTimeMs).sum();
        }

        @Override
        public long getAggregateBuildErrorCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getBuildErrorCount).sum();
        }

        @Override
        public long getAggregateRenderCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getRenderCount).sum();
        }

        @Override
        public long getAggregateTotalRenderTimeMs() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getTotalRenderTimeMs).sum();
        }

        @Override
        public long getAggregateRenderErrorCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getRenderErrorCount).sum();
        }

        // ---- 一覧 ----

        @Override
        public String[] listSystemIDs() {
            return _statsMap.asMap().keySet().toArray(new String[0]);
        }

        @Override
        public String[] listPageSystemIDs() {
            return _statsMap.asMap().values().stream()
                    .filter(s -> SpecificationStats.TYPE_PAGE.equals(s.getType()))
                    .map(SpecificationStats::getSystemID)
                    .toArray(String[]::new);
        }

        @Override
        public String[] listTemplateSystemIDs() {
            return _statsMap.asMap().values().stream()
                    .filter(s -> SpecificationStats.TYPE_TEMPLATE.equals(s.getType()))
                    .map(SpecificationStats::getSystemID)
                    .toArray(String[]::new);
        }

        // ---- ビルド統計 ----

        @Override
        public long getBuildCount(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getBuildCount() : 0L;
        }

        @Override
        public long getTotalBuildTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getTotalBuildTimeMs() : 0L;
        }

        @Override
        public double getAvgBuildTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getAvgBuildTimeMs() : 0.0;
        }

        @Override
        public long getMaxBuildTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getMaxBuildTimeMs() : 0L;
        }

        @Override
        public long getLastBuildTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getLastBuildTimeMs() : 0L;
        }

        @Override
        public long getBuildErrorCount(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getBuildErrorCount() : 0L;
        }

        @Override
        public long getLastBuiltAt(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getLastBuiltAt() : 0L;
        }

        // ---- レンダリング統計 ----

        @Override
        public long getRenderCount(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getRenderCount() : 0L;
        }

        @Override
        public long getTotalRenderTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getTotalRenderTimeMs() : 0L;
        }

        @Override
        public double getAvgRenderTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getAvgRenderTimeMs() : 0.0;
        }

        @Override
        public long getMaxRenderTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getMaxRenderTimeMs() : 0L;
        }

        @Override
        public long getLastRenderTimeMs(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getLastRenderTimeMs() : 0L;
        }

        @Override
        public long getRenderErrorCount(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getRenderErrorCount() : 0L;
        }

        @Override
        public long getLastRenderedAt(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getLastRenderedAt() : 0L;
        }

        @Override
        public String getType(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.getType() : null;
        }

        // ---- JSON 出力 ----

        @Override
        public String getSummaryAsJson() {
            Collection<SpecificationStats> all = getAll();
            StringBuilder sb = new StringBuilder();
            sb.append("{");

            // aggregate
            sb.append("\"aggregate\":{");
            sb.append("\"trackedCount\":").append(all.size()).append(",");
            sb.append("\"totalBuildCount\":").append(
                    all.stream().mapToLong(SpecificationStats::getBuildCount).sum()).append(",");
            sb.append("\"totalBuildTimeMs\":").append(
                    all.stream().mapToLong(SpecificationStats::getTotalBuildTimeMs).sum()).append(",");
            sb.append("\"totalBuildErrorCount\":").append(
                    all.stream().mapToLong(SpecificationStats::getBuildErrorCount).sum()).append(",");
            sb.append("\"totalRenderCount\":").append(
                    all.stream().mapToLong(SpecificationStats::getRenderCount).sum()).append(",");
            sb.append("\"totalRenderTimeMs\":").append(
                    all.stream().mapToLong(SpecificationStats::getTotalRenderTimeMs).sum()).append(",");
            sb.append("\"totalRenderErrorCount\":").append(
                    all.stream().mapToLong(SpecificationStats::getRenderErrorCount).sum());
            sb.append("},");

            // per-spec list
            sb.append("\"specifications\":[");
            boolean first = true;
            for (SpecificationStats stats : all) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(stats.toJson());
                first = false;
            }
            sb.append("]");

            sb.append("}");
            return sb.toString();
        }

        @Override
        public String getSpecificationAsJson(String systemID) {
            SpecificationStats s = _statsMap.getIfPresent(systemID);
            return s != null ? s.toJson() : "null";
        }

        @Override
        public String getTopSlowRendersAsJson(int topN) {
            List<SpecificationStats> sorted = _statsMap.asMap().values().stream()
                    .filter(s -> s.getRenderCount() > 0)
                    .sorted(Comparator.comparingDouble(SpecificationStats::getAvgRenderTimeMs)
                            .reversed())
                    .limit(topN)
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (SpecificationStats stats : sorted) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(stats.toJson());
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }

        // ---- 管理操作 ----

        @Override
        public void reset(String systemID) {
            SpecificationProfileRegistry.this.reset(systemID);
        }

        @Override
        public void resetAll() {
            SpecificationProfileRegistry.this.resetAll();
        }

        // ---- 診断イベント統計 ----

        @Override
        public long getAggregateDiagWarnCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getTotalDiagWarnCount).sum();
        }

        @Override
        public long getAggregateDiagErrorCount() {
            return _statsMap.asMap().values().stream()
                    .mapToLong(SpecificationStats::getTotalDiagErrorCount).sum();
        }

        // ---- フィルタリング一覧 ----

        @Override
        public String[] listSystemIDsWithErrors() {
            return _statsMap.asMap().values().stream()
                    .filter(SpecificationStats::hasAnyErrors)
                    .map(SpecificationStats::getSystemID)
                    .toArray(String[]::new);
        }

        @Override
        public String[] listSystemIDsWithWarnings() {
            return _statsMap.asMap().values().stream()
                    .filter(SpecificationStats::hasDiagnosticIssues)
                    .map(SpecificationStats::getSystemID)
                    .toArray(String[]::new);
        }

        @Override
        public String getTopByDiagEventsAsJson(int topN) {
            List<SpecificationStats> sorted = _statsMap.asMap().values().stream()
                    .filter(s -> s.getTotalDiagWarnCount() + s.getTotalDiagErrorCount() > 0)
                    .sorted(Comparator.comparingLong(
                            (SpecificationStats s) ->
                                    s.getTotalDiagWarnCount() + s.getTotalDiagErrorCount())
                            .reversed())
                    .limit(topN)
                    .collect(Collectors.toList());

            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (SpecificationStats stats : sorted) {
                if (!first) sb.append(",");
                sb.append(stats.toJson());
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
    }
}

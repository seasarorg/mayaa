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
package org.seasar.mayaa.management;

import javax.management.MXBean;

/**
 * キャッシュ上に保持されている Template および Page ごとの
 * プロファイリング情報（ビルド時間・レンダリング時間・エラー回数・呼び出し回数など）
 * を提供する MXBean インタフェース。
 *
 * <p>ObjectName: {@value #JMX_OBJECT_NAME_FORMAT}</p>
 *
 * <p>ページ単位の詳細データと、全体の集計値の両方にアクセスできる。
 * JSON 形式の出力メソッドは HTTP エンドポイント等の外部 API から
 * 利用することを想定している。</p>
 *
 * @since 2.0
 * @author Watanabe, Mitsutaka
 */
@MXBean
public interface SpecificationProfileMXBean {

    /** JMX ObjectName */
    static final String JMX_OBJECT_NAME_FORMAT = "org.seasar.mayaa:type=SpecificationProfile";

    // ----------------------------------------------------------------
    // 集計値 (全 Specification の合算)
    // ----------------------------------------------------------------

    /** 現在プロファイリングが記録されている Specification の総数 */
    int getTrackedCount();

    /** 全 Specification のビルド合計回数 */
    long getAggregateBuildCount();

    /** 全 Specification のビルド合計時間 (ms) */
    long getAggregateTotalBuildTimeMs();

    /** 全 Specification のビルドエラー合計回数 */
    long getAggregateBuildErrorCount();

    /** 全 Specification のレンダリング合計回数 */
    long getAggregateRenderCount();

    /** 全 Specification のレンダリング合計時間 (ms) */
    long getAggregateTotalRenderTimeMs();

    /** 全 Specification のレンダリングエラー合計回数 */
    long getAggregateRenderErrorCount();

    // ----------------------------------------------------------------
    // Specification 一覧の取得
    // ----------------------------------------------------------------

    /** プロファイリングされている全 systemID の一覧 */
    String[] listSystemIDs();

    /** ページ ("page") の systemID 一覧 */
    String[] listPageSystemIDs();

    /** テンプレート ("template") の systemID 一覧 */
    String[] listTemplateSystemIDs();

    // ----------------------------------------------------------------
    // Specification ごとのビルド統計
    // ----------------------------------------------------------------

    /** 指定 systemID のビルド回数 */
    long getBuildCount(String systemID);

    /** 指定 systemID のビルド合計時間 (ms) */
    long getTotalBuildTimeMs(String systemID);

    /** 指定 systemID のビルド平均時間 (ms) */
    double getAvgBuildTimeMs(String systemID);

    /** 指定 systemID のビルド最大時間 (ms) */
    long getMaxBuildTimeMs(String systemID);

    /** 指定 systemID の最終ビルド時間 (ms) */
    long getLastBuildTimeMs(String systemID);

    /** 指定 systemID のビルドエラー回数 */
    long getBuildErrorCount(String systemID);

    /** 指定 systemID の最終ビルド日時 (epoch millis, 0 = 未ビルド) */
    long getLastBuiltAt(String systemID);

    // ----------------------------------------------------------------
    // Specification ごとのレンダリング統計
    // ----------------------------------------------------------------

    /** 指定 systemID のレンダリング回数 */
    long getRenderCount(String systemID);

    /** 指定 systemID のレンダリング合計時間 (ms) */
    long getTotalRenderTimeMs(String systemID);

    /** 指定 systemID のレンダリング平均時間 (ms) */
    double getAvgRenderTimeMs(String systemID);

    /** 指定 systemID のレンダリング最大時間 (ms) */
    long getMaxRenderTimeMs(String systemID);

    /** 指定 systemID の最終レンダリング時間 (ms) */
    long getLastRenderTimeMs(String systemID);

    /** 指定 systemID のレンダリングエラー回数 */
    long getRenderErrorCount(String systemID);

    /** 指定 systemID の最終レンダリング日時 (epoch millis, 0 = 未レンダリング) */
    long getLastRenderedAt(String systemID);

    /** 指定 systemID の種別 ("page" / "template") */
    String getType(String systemID);

    // ----------------------------------------------------------------
    // JSON 形式の出力 (HTTP エンドポイント等との連携用)
    // ----------------------------------------------------------------

    /**
     * 全 Specification のプロファイリングデータを JSON 形式で返す。
     * 外部 HTTP API からの一括取得に利用する。
     */
    String getSummaryAsJson();

    /**
     * 指定 systemID の Specification のプロファイリングデータを JSON 形式で返す。
     * 該当する Specification が存在しない場合は {@code "null"} を返す。
     */
    String getSpecificationAsJson(String systemID);

    /**
     * レンダリング平均時間が長い順に上位 N 件の systemID を JSON 形式で返す。
     *
     * @param topN 取得件数
     */
    String getTopSlowRendersAsJson(int topN);

    // ----------------------------------------------------------------
    // 管理操作
    // ----------------------------------------------------------------

    /**
     * 指定 systemID のプロファイリングデータをリセットする。
     * 存在しない systemID の場合は何もしない。
     */
    void reset(String systemID);

    /** 全 Specification のプロファイリングデータをリセットする */
    void resetAll();

    // ----------------------------------------------------------------
    // 診断イベント統計 (DiagnosticEventBuffer 連携)
    // ----------------------------------------------------------------

    /** 全 Specification のフェーズ横断診断 WARN 合計件数 */
    long getAggregateDiagWarnCount();

    /** 全 Specification のフェーズ横断診断 ERROR 合計件数 */
    long getAggregateDiagErrorCount();

    // ----------------------------------------------------------------
    // フィルタリング一覧
    // ----------------------------------------------------------------

    /** 診断 ERROR が 1 件以上ある Specification の systemID 一覧 */
    String[] listSystemIDsWithErrors();

    /** 診断 WARN が 1 件以上ある Specification の systemID 一覧 */
    String[] listSystemIDsWithWarnings();

    /**
     * 診断イベント (warn+error) が多い順に上位 N 件を JSON 形式で返す。
     * 各エントリにはフェーズ別 (parse/build/render) の内訳も含む。
     *
     * @param topN 取得件数
     */
    String getTopByDiagEventsAsJson(int topN);
}


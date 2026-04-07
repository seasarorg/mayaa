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
import java.util.List;
import java.util.function.Consumer;

import org.seasar.mayaa.PositionAware;

/**
 * パース・ビルド・レンダリング各フェーズの診断イベントを配信するイベントバス。
 * イベントデータは保管しない。受信・保管・ログ出力はリスナー側で行う。
 */
public final class DiagnosticEventBuffer {

    public enum Phase {
        PARSE,
        BUILD,
        RENDER
    }

    public enum Level {
        WARN,
        ERROR
    }

    public static record Event(
            long timestampMillis,
            Phase phase,
            Level level,
            String label,
            String source,
            String message,
            String scriptText,
            String sample,
            String positionSystemID,
            int positionLineNumber,
            boolean positionOnTemplate,
            /** 記録時点でのスコープ systemID (ビルド中の spec など)。null 可。 */
            String ownerSystemID) {
    }

    public static final int UNKNOWN_POSITION_LINE = -1;

    private static final List<Consumer<Event>> _listeners = new ArrayList<>();

    /**
     * 現在のスレッドで処理中の Specification の systemID を保持するスコープ。
     * ビルド・レンダリングフェーズの開始時に {@link #beginScope(String)} で設定し、
     * 終了時に {@link #endScope()} でクリアする。
     * この値は {@link Event#ownerSystemID()} として記録時にキャプチャされる。
     */
    private static final ThreadLocal<String> _scopeSystemID = new ThreadLocal<>();

    private DiagnosticEventBuffer() {
        // no instantiation
    }

    /**
     * 現在のスレッドで処理中の Specification のスコープを開始する。
     * 診断イベント記録時に {@link Event#ownerSystemID()} としてキャプチャされる。
     * 必ず対応する {@link #endScope()} を finally で呼び出すこと。
     */
    public static void beginScope(String systemID) {
        _scopeSystemID.set(systemID);
    }

    /** 現在スレッドのスコープを終了し、スレッドローカルをクリアする。 */
    public static void endScope() {
        _scopeSystemID.remove();
    }

    /**
     * イベント発行時に呼び出されるリスナーを登録する。
     * リスナー内で例外が発生してもイベント記録は妨げない。
     */
    public static synchronized void addListener(Consumer<Event> listener) {
        _listeners.add(listener);
    }

    /** 登録済みリスナーを解除する。未登録の場合は何もしない。 */
    public static synchronized void removeListener(Consumer<Event> listener) {
        _listeners.remove(listener);
    }

    public static synchronized void recordWarn(Phase phase,
            String label,
            String source,
            String message,
            String sample) {
        recordWarn(phase, label, source, message, null, sample, null);
    }

    public static synchronized void recordWarn(Phase phase,
            String label,
            String source,
            String message,
            String scriptText,
            String sample,
            PositionAware position) {
        record(new Event(System.currentTimeMillis(), phase, Level.WARN,
                label, source, message, scriptText, sample,
                position != null ? position.getSystemID() : null,
                position != null ? position.getLineNumber() : UNKNOWN_POSITION_LINE,
                position != null && position.isOnTemplate(),
                _scopeSystemID.get()));
    }

    public static synchronized void recordError(Phase phase,
            String label,
            String source,
            String message,
            String sample) {
        recordError(phase, label, source, message, null, sample, null);
    }

    public static synchronized void recordError(Phase phase,
            String label,
            String source,
            String message,
            String scriptText,
            String sample,
            PositionAware position) {
        record(new Event(System.currentTimeMillis(), phase, Level.ERROR,
                label, source, message, scriptText, sample,
                position != null ? position.getSystemID() : null,
                position != null ? position.getLineNumber() : UNKNOWN_POSITION_LINE,
                position != null && position.isOnTemplate(),
                _scopeSystemID.get()));
    }

    private static void record(Event event) {
        for (Consumer<Event> listener : _listeners) {
            try {
                listener.accept(event);
            } catch (Exception ignored) {
                // リスナーの失敗でイベント配信自体を妨げない
            }
        }
    }
}
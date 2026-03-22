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

import org.seasar.mayaa.PositionAware;

/**
 * 管理画面向けに、各フェーズの警告・エラーをリングバッファで保持する。
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
            boolean positionOnTemplate) {
    }

    public static final int UNKNOWN_POSITION_LINE = -1;

    private static final int DEFAULT_CAPACITY = 512;
    private static final Deque<Event> _events = new ArrayDeque<Event>(DEFAULT_CAPACITY);

    private DiagnosticEventBuffer() {
        // no instantiation
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
                position != null && position.isOnTemplate()));
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
                position != null && position.isOnTemplate()));
    }

    private static void record(Event event) {
        while (_events.size() >= DEFAULT_CAPACITY) {
            _events.removeFirst();
        }
        _events.addLast(event);
    }

    public static synchronized List<Event> snapshot() {
        return new ArrayList<Event>(_events);
    }

    public static synchronized int size() {
        return _events.size();
    }

    public static synchronized void clear() {
        _events.clear();
    }
}
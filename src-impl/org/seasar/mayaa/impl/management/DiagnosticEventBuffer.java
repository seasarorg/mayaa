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

    public static final class Event {
        private final long _timestampMillis;
        private final Phase _phase;
        private final Level _level;
        private final String _label;
        private final String _source;
        private final String _message;
        private final String _sample;

        Event(long timestampMillis, Phase phase, Level level,
                String label, String source, String message, String sample) {
            _timestampMillis = timestampMillis;
            _phase = phase;
            _level = level;
            _label = label;
            _source = source;
            _message = message;
            _sample = sample;
        }

        public long getTimestampMillis() {
            return _timestampMillis;
        }

        public Phase getPhase() {
            return _phase;
        }

        public Level getLevel() {
            return _level;
        }

        public String getLabel() {
            return _label;
        }

        public String getSource() {
            return _source;
        }

        public String getMessage() {
            return _message;
        }

        public String getSample() {
            return _sample;
        }
    }

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
        record(new Event(System.currentTimeMillis(), phase, Level.WARN,
                label, source, message, sample));
    }

    public static synchronized void recordError(Phase phase,
            String label,
            String source,
            String message,
            String sample) {
        record(new Event(System.currentTimeMillis(), phase, Level.ERROR,
                label, source, message, sample));
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
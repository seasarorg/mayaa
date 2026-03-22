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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DiagnosticEventBufferTest {

    @BeforeEach
    public void setUp() {
        DiagnosticEventBuffer.clear();
    }

    @AfterEach
    public void tearDown() {
        DiagnosticEventBuffer.clear();
    }

    @Test
    public void testRecordWarn_snapshotContainsEvent() {
        DiagnosticEventBuffer.recordWarn(DiagnosticEventBuffer.Phase.RENDER,
                "auto-escape",
                "test.Source", "warn message", "sample value");

        List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
        assertEquals(1, events.size());
        DiagnosticEventBuffer.Event event = events.get(0);
        assertEquals(DiagnosticEventBuffer.Phase.RENDER, event.phase());
        assertEquals(DiagnosticEventBuffer.Level.WARN, event.level());
        assertEquals("auto-escape", event.label());
        assertEquals("test.Source", event.source());
        assertEquals("warn message", event.message());
        assertEquals(null, event.scriptText());
        assertEquals("sample value", event.sample());
        assertEquals(null, event.positionSystemID());
        assertEquals(DiagnosticEventBuffer.UNKNOWN_POSITION_LINE,
            event.positionLineNumber());
        assertEquals(false, event.positionOnTemplate());
    }
}
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
package org.seasar.mayaa.impl.engine.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.cycle.script.RawOutputCompiledScript;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.util.EscapeUtil;

public class CharactersProcessorTest {

    @Test
    public void testApplyHtmlBodyAutoEscape_dynamicHtmlBody() {
        String escaped = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom & Jerry</b>", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("&lt;b&gt;Tom &amp; Jerry&lt;/b&gt;", escaped);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_dynamicHtmlBody_quoteIsNotEscaped() {
        String escaped = CharactersProcessor.applyHtmlBodyAutoEscape(
                "\"Tom\" & Jerry", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("\"Tom\" &amp; Jerry", escaped);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_disabled() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom</b>", new NonLiteralScript(), false,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("<b>Tom</b>", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_literalOrRaw() {
        String literal = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom</b>", new LiteralScript("<b>Tom</b>"), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("<b>Tom</b>", literal);

        String raw = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom</b>", new RawOutputCompiledScript(new NonLiteralScript()), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("<b>Tom</b>", raw);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_script() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "O'Reilly\n", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.SCRIPT);
        assertEquals("O\\'Reilly\\n", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_scriptLiteralIsNotEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "O'Reilly\n", new LiteralScript("O'Reilly\\n"), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.SCRIPT);
        assertEquals("O'Reilly\n", output);
    }

        @Test
        public void testApplyHtmlBodyAutoEscapePerBlock_scriptMixed() {
        CompiledScript[] scripts = new CompiledScript[] {
            new LiteralScript("var v = '"),
            new FixedResultScript("O'Reilly"),
            new LiteralScript("';")
        };
        String output = CharactersProcessor.applyHtmlBodyAutoEscapePerBlock(
            scripts, String.class, true,
            EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.SCRIPT);
        assertEquals("var v = 'O\\'Reilly';", output);
        }

        @Test
        public void testApplyHtmlBodyAutoEscapePerBlock_scriptRawIsNotEscaped() {
        CompiledScript[] scripts = new CompiledScript[] {
            new LiteralScript("var data = "),
            new RawOutputCompiledScript(new FixedResultScript("{\"k\":\"A'B\"}")),
            new LiteralScript(";")
        };
        String output = CharactersProcessor.applyHtmlBodyAutoEscapePerBlock(
            scripts, String.class, true,
            EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.SCRIPT);
        assertEquals("var data = {\"k\":\"A'B\"};", output);
        }

            @Test
            public void testApplyHtmlBodyAutoEscapePerBlock_voidExpectedClassReturnsNull() {
                int[] count = new int[] { 0 };
            CompiledScript[] scripts = new CompiledScript[] {
                    new CountingScript(count),
                    new CountingScript(count)
            };
            String output = CharactersProcessor.applyHtmlBodyAutoEscapePerBlock(
                scripts, Void.class, true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.SCRIPT);
            assertEquals(null, output);
                assertEquals(2, count[0]);
            }

    @Test
    public void testApplyHtmlBodyAutoEscape_style() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "\"x\"\n", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.STYLE);
        assertEquals("\\\"x\\\"\\00000A ", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_textareaPre() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom & Jerry</b>", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.TEXTAREA_PRE);
        assertEquals("&lt;b&gt;Tom &amp; Jerry&lt;/b&gt;", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_alreadyEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "&lt;b&gt;Tom&lt;/b&gt;", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);
        assertEquals("&lt;b&gt;Tom&lt;/b&gt;", output);
    }

    // ---- HTML_ATTRIBUTE ----

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_dynamicEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<script>alert('xss')</script>", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_doubleQuoteEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "Tom & \"Jerry\"", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("Tom &amp; &quot;Jerry&quot;", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_disabledReturnsUnescaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "Tom & <b>Jerry</b>", new NonLiteralScript(), false,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("Tom & <b>Jerry</b>", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_literalNotEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "Tom & <b>Jerry</b>",
                new LiteralScript("Tom & <b>Jerry</b>"), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("Tom & <b>Jerry</b>", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_alreadyEscapedSkipped() {
        // isEscaped() が true と判定する &amp; を含む値 → エスケープスキップ
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "Tom &amp; Jerry", new NonLiteralScript(), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("Tom &amp; Jerry", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_htmlAttribute_rawOutputNotEscaped() {
        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>safe html</b>",
                new RawOutputCompiledScript(new NonLiteralScript()), true,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_ATTRIBUTE);
        assertEquals("<b>safe html</b>", output);
    }

    @Test
    public void testApplyHtmlBodyAutoEscape_autoEscapeDisabled_recordsRenderWarningEvent() {
        DiagnosticEventBuffer.clear();

        String output = CharactersProcessor.applyHtmlBodyAutoEscape(
                "<b>Tom</b>", new NonLiteralScript(), false,
                EscapeUtil.DETECTION_LEVEL_NORMAL, OutputContext.HTML_BODY);

        assertEquals("<b>Tom</b>", output);

        List<DiagnosticEventBuffer.Event> events = DiagnosticEventBuffer.snapshot();
        assertEquals(1, events.size());
        DiagnosticEventBuffer.Event event = events.get(0);
        assertEquals(DiagnosticEventBuffer.Phase.RENDER, event.getPhase());
        assertEquals(DiagnosticEventBuffer.Level.WARN, event.getLevel());
        assertEquals("auto-escape", event.getLabel());
        assertEquals("org.seasar.mayaa.impl.engine.processor.CharactersProcessor", event.getSource());
    }

    static class NonLiteralScript implements CompiledScript {
        private static final long serialVersionUID = 1L;

        public String getScriptText() {
            return "expr";
        }

        public boolean isLiteral() {
            return false;
        }

        public Object execute(Class<?> expectedClass, Object[] args) {
            return "";
        }

        public void setMethodArgClasses(Class<?>[] methodArgClasses) {
            // no-op
        }

        public Class<?>[] getMethodArgClasses() {
            return null;
        }

        public boolean isReadOnly() {
            return true;
        }

        public void assignValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    static class FixedResultScript implements CompiledScript {
        private static final long serialVersionUID = 1L;
        private final String _result;

        FixedResultScript(String result) {
            _result = result;
        }

        public String getScriptText() {
            return "expr";
        }

        public boolean isLiteral() {
            return false;
        }

        public Object execute(Class<?> expectedClass, Object[] args) {
            return _result;
        }

        public void setMethodArgClasses(Class<?>[] methodArgClasses) {
            // no-op
        }

        public Class<?>[] getMethodArgClasses() {
            return null;
        }

        public boolean isReadOnly() {
            return true;
        }

        public void assignValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }

    static class CountingScript implements CompiledScript {
        private static final long serialVersionUID = 1L;
        private final int[] _count;

        CountingScript(int[] count) {
            _count = count;
        }

        public String getScriptText() {
            return "expr";
        }

        public boolean isLiteral() {
            return false;
        }

        public Object execute(Class<?> expectedClass, Object[] args) {
            _count[0]++;
            if (expectedClass == Void.class) {
                return null;
            }
            return "x";
        }

        public void setMethodArgClasses(Class<?>[] methodArgClasses) {
            // no-op
        }

        public Class<?>[] getMethodArgClasses() {
            return null;
        }

        public boolean isReadOnly() {
            return true;
        }

        public void assignValue(Object value) {
            throw new UnsupportedOperationException();
        }
    }
}

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

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.impl.cycle.script.LiteralScript;
import org.seasar.mayaa.impl.cycle.script.RawOutputCompiledScript;

public class ElementProcessorTest {

    @Test
    public void testApplyHtmlAttributeAutoEscape_enabled() {
        String escaped = ElementProcessor.applyHtmlAttributeAutoEscape(
                "\"x\" & <tag>", new NonLiteralScript(), true,
                true, OutputContext.HTML_ATTRIBUTE);
        assertEquals("&quot;x&quot; &amp; &lt;tag&gt;", escaped);
    }

    @Test
    public void testApplyHtmlAttributeAutoEscape_enabledWithoutAmp() {
        String escaped = ElementProcessor.applyHtmlAttributeAutoEscape(
                "\"x\" & <tag>", new NonLiteralScript(), true,
                false, OutputContext.HTML_ATTRIBUTE);
        assertEquals("&quot;x&quot; & &lt;tag&gt;", escaped);
    }

    @Test
    public void testApplyHtmlAttributeAutoEscape_disabledUsesUnifiedEscaping() {
        String escaped = ElementProcessor.applyHtmlAttributeAutoEscape(
                "\"x\" & <tag> '", new NonLiteralScript(), false,
                true, OutputContext.HTML_ATTRIBUTE);
        assertEquals("&quot;x&quot; &amp; &lt;tag&gt; &#39;", escaped);
    }

    @Test
    public void testApplyHtmlAttributeAutoEscape_literalOrRaw() {
        String literal = ElementProcessor.applyHtmlAttributeAutoEscape(
                "<tag>", new LiteralScript("<tag>"), true,
                true, OutputContext.HTML_ATTRIBUTE);
        assertEquals("<tag>", literal);

        String raw = ElementProcessor.applyHtmlAttributeAutoEscape(
                "<tag>", new RawOutputCompiledScript(new NonLiteralScript()), true,
                true, OutputContext.HTML_ATTRIBUTE);
        assertEquals("<tag>", raw);
    }

    @Test
    public void testApplyHtmlAttributeAutoEscape_alreadyEscaped() {
        String escaped = ElementProcessor.applyHtmlAttributeAutoEscape(
                "&quot;x&quot;", new NonLiteralScript(), true,
                true, OutputContext.HTML_ATTRIBUTE);
        assertEquals("&quot;x&quot;", escaped);
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
}

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
package org.seasar.mayaa.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EscapeUtilTest {

    @Test
    public void testEscapeHtml() {
        assertEquals("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;",
                EscapeUtil.escapeHtml("<script>alert('x')</script>"));
    }

    @Test
    public void testEscapeJavaScriptString() {
        assertEquals("line1\\nline2\\t\\\"q\\\"",
                EscapeUtil.escapeJavaScriptString("line1\nline2\t\"q\""));
    }

    @Test
    public void testEscapeCssString() {
        assertEquals("\\'x\\'\\00000A ", EscapeUtil.escapeCssString("'x'\n"));
    }

    @Test
    public void testIsEscapedNormal() {
        assertTrue(EscapeUtil.isEscaped("&lt;b&gt;"));
        assertTrue(EscapeUtil.isEscaped("\\\"quoted\\\""));
        assertFalse(EscapeUtil.isEscaped("plain"));
    }

    @Test
    public void testIsEscapedIgnoresLevelAndUsesNormalRule() {
        assertTrue(EscapeUtil.isEscaped("&lt;b&gt;"));
        assertTrue(EscapeUtil.isEscaped("&lt;b&gt; and \\\"quoted\\\""));
    }
}

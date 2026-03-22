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

import java.util.regex.Pattern;

/**
 * 自動エスケープ機能で利用する文字列エスケープ/検出ユーティリティ。
 */
public class EscapeUtil {

    private static final Pattern HTML_ESCAPE_PATTERN = Pattern.compile(
            "&(?:lt|gt|quot|apos|amp|#39|#[0-9]+|#x[0-9A-Fa-f]+);");
    private static final Pattern BACKSLASH_ESCAPE_PATTERN = Pattern.compile(
            "\\\\(?:[\\\"'ntrbf/0]|u[0-9A-Fa-f]{4}|x[0-9A-Fa-f]{2})");

    private EscapeUtil() {
        // no instantiation
    }

    public static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return StringUtil.escapeXml(value).replace("'", "&#39;");
    }

    public static String escapeHtmlBody(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String escapeHtmlWithoutAmp(String value) {
        if (value == null) {
            return "";
        }
        return StringUtil.escapeXmlWithoutAmp(value).replace("'", "&#39;");
    }

    public static String escapeJavaScriptString(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\\'");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c == '\u2028' || c == '\u2029') {
                        sb.append(String.format("\\u%04X", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static String escapeCssString(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\n':
                    sb.append("\\00000A ");
                    break;
                case '\r':
                    sb.append("\\00000D ");
                    break;
                case '\f':
                    sb.append("\\00000C ");
                    break;
                case '\t':
                    sb.append("\\000009 ");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean isEscaped(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }
        boolean htmlEscaped = HTML_ESCAPE_PATTERN.matcher(value).find();
        boolean slashEscaped = BACKSLASH_ESCAPE_PATTERN.matcher(value).find();
        return htmlEscaped || slashEscaped;
    }
}

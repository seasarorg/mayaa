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
package org.seasar.mayaa.impl.cycle.script;

/**
 * Rewrites MAYAA_SCOPE macros into ordinary script blocks.
 */
final class ScriptScopeMacroRewriter {

    private static final String MACRO_SCOPE = "MAYAA_SCOPE";
    private static final String MACRO_SCOPE_AS_STRING = "MAYAA_SCOPE_AS_STRING";
    private static final String MACRO_SCOPE_RAW = "MAYAA_SCOPE_RAW";

    private ScriptScopeMacroRewriter() {
        // no instantiation
    }

    static String rewriteToScriptBlocks(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (!hasMacroCandidate(text)) {
            return text;
        }

        StringBuilder out = new StringBuilder(text.length() + 32);
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            char prev = i > 0 ? text.charAt(i - 1) : '\0';
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';

            if (inLineComment) {
                out.append(c);
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                i++;
                continue;
            }
            if (inBlockComment) {
                out.append(c);
                if (prev == '*' && c == '/') {
                    inBlockComment = false;
                }
                i++;
                continue;
            }
            if (inSingleQuote) {
                out.append(c);
                if (c == '\'' && prev != '\\') {
                    inSingleQuote = false;
                }
                i++;
                continue;
            }
            if (inDoubleQuote) {
                out.append(c);
                if (c == '"' && prev != '\\') {
                    inDoubleQuote = false;
                }
                i++;
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '\'') {
                inSingleQuote = true;
                out.append(c);
                i++;
                continue;
            }
            if (c == '"') {
                inDoubleQuote = true;
                out.append(c);
                i++;
                continue;
            }

            MacroMatch match = findMacroAt(text, i);
            if (match == null) {
                out.append(c);
                i++;
                continue;
            }

            int openParen = skipWhitespace(text, i + match.name.length());
            ParenthesisRange range = findArgumentRange(text, openParen);
            if (range == null) {
                out.append(c);
                i++;
                continue;
            }

            String expression = text.substring(range.argStart, range.argEnd);
            out.append(toScriptBlock(match.type, expression));
            i = range.closeParen + 1;
        }

        return out.toString();
    }

    private static boolean hasMacroCandidate(String text) {
        return text.indexOf(MACRO_SCOPE) >= 0;
    }

    private static String toScriptBlock(MacroType type, String expression) {
        switch (type) {
            case SCOPE_RAW:
                return "${=" + expression + "}";
            case SCOPE_AS_STRING:
                return "${String(" + expression + ")}";
            case SCOPE:
            default:
                return "${" + expression + "}";
        }
    }

    private static MacroMatch findMacroAt(String text, int index) {
        MacroMatch asString = matchMacro(text, index, MACRO_SCOPE_AS_STRING, MacroType.SCOPE_AS_STRING);
        if (asString != null) {
            return asString;
        }
        MacroMatch raw = matchMacro(text, index, MACRO_SCOPE_RAW, MacroType.SCOPE_RAW);
        if (raw != null) {
            return raw;
        }
        return matchMacro(text, index, MACRO_SCOPE, MacroType.SCOPE);
    }

    private static MacroMatch matchMacro(String text, int index, String name, MacroType type) {
        if (!text.startsWith(name, index)) {
            return null;
        }
        int before = index - 1;
        if (before >= 0 && isIdentifierPart(text.charAt(before))) {
            return null;
        }
        int afterName = index + name.length();
        int openParen = skipWhitespace(text, afterName);
        if (openParen >= text.length() || text.charAt(openParen) != '(') {
            return null;
        }
        return new MacroMatch(name, type);
    }

    private static ParenthesisRange findArgumentRange(String text, int openParenIndex) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;

        for (int i = openParenIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            char prev = i > 0 ? text.charAt(i - 1) : '\0';
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';

            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (prev == '*' && c == '/') {
                    inBlockComment = false;
                }
                continue;
            }
            if (inSingleQuote) {
                if (c == '\'' && prev != '\\') {
                    inSingleQuote = false;
                }
                continue;
            }
            if (inDoubleQuote) {
                if (c == '"' && prev != '\\') {
                    inDoubleQuote = false;
                }
                continue;
            }

            if (c == '/' && next == '/') {
                inLineComment = true;
                i++;
                continue;
            }
            if (c == '/' && next == '*') {
                inBlockComment = true;
                i++;
                continue;
            }
            if (c == '\'') {
                inSingleQuote = true;
                continue;
            }
            if (c == '"') {
                inDoubleQuote = true;
                continue;
            }
            if (c == '(') {
                depth++;
                continue;
            }
            if (c == ')') {
                depth--;
                if (depth == 0) {
                    return new ParenthesisRange(openParenIndex + 1, i, i);
                }
            }
        }
        return null;
    }

    private static int skipWhitespace(String text, int index) {
        int i = index;
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return i;
    }

    private static boolean isIdentifierPart(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }

    private enum MacroType {
        SCOPE,
        SCOPE_AS_STRING,
        SCOPE_RAW
    }

    private static class MacroMatch {
        private final String name;
        private final MacroType type;

        private MacroMatch(String name, MacroType type) {
            this.name = name;
            this.type = type;
        }
    }

    private static class ParenthesisRange {
        private final int argStart;
        private final int argEnd;
        private final int closeParen;

        private ParenthesisRange(int argStart, int argEnd, int closeParen) {
            this.argStart = argStart;
            this.argEnd = argEnd;
            this.closeParen = closeParen;
        }
    }
}
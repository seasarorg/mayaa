/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockIterator implements Iterator {

    private String _text;
    private String _blockSign;
    private int _offset;
    private boolean _onTemplate = true;

    public ScriptBlockIterator(
            String text, String blockSign, boolean onTemplate) {
        if (StringUtil.isEmpty(text) || StringUtil.isEmpty(blockSign)) {
            throw new IllegalArgumentException();
        }
        _text = text;
        _blockSign = blockSign;
        _offset = 0;
        _onTemplate = onTemplate;
    }

    public boolean hasNext() {
        return _offset < _text.length();
    }

    protected int scanBlockCloseOffset(int start) {
        char c = _text.charAt(start);

        if (c != '{') {
            throw new IllegalArgumentException();
        }

        boolean inBlockComment = false;
        boolean inLineComment = false;
        boolean inString = false;
        char stringBeginQuote = '\0';
        boolean inEscapeSequence = false;
        int depth = 0;
        for (int i = start; i < _text.length(); i++) {
            c = _text.charAt(i);
            if (inBlockComment) {
                if (c != '/' || _text.charAt(i - 1) != '*') {
                    continue;
                }
                inBlockComment = false;
            } else if (inLineComment) {
                if (c != '\n' && c != '\r') {
                    continue;
                }
                inLineComment = false;
            } else if (inEscapeSequence) {
                inEscapeSequence = false;
                continue;
            } else if (inString && stringBeginQuote == c) {
                inString = false;
                continue;
            }

            if (inString == false) {
                if (c == '/') {
                    if (i > 0 && _text.charAt(i - 1) == '/') {
                        inLineComment = true;
                        continue;
                    }
                } else if (c == '*') {
                    if (i > 0 && _text.charAt(i - 1) == '/') {
                        inBlockComment = true;
                        continue;
                    }
                } else if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    } else if (depth < 0) {
                        throw new UnbalancedBraceException(_text, i);
                    }
                } else if (c == '\'' || c == '"') {
                    inString = true;
                    stringBeginQuote = c;
                }
            } else if (c == '\\') {
                inEscapeSequence = true;
            }
        }
        return -1;
    }

    public Object next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        String blockStart = _blockSign + "{";
        int sign = _text.indexOf(blockStart, _offset);
        if (sign != -1) {
            if (_offset == sign) {
                // script block
                int close = scanBlockCloseOffset(_offset + _blockSign.length());
                if (close == -1) {
                    throw new UnbalancedBraceException(_text, _text.length());
                }
                String text = _text.substring(_offset + blockStart.length(), close);
                _offset = close + 1;
                if (_onTemplate) {
                    text = StringUtil.resolveEntity(text);
                }
                return new ScriptBlock(text, false, _blockSign);
            }
            // literal
            String lastLiteralBlock = _text.substring(_offset, sign);
            _offset = sign;
            return new ScriptBlock(lastLiteralBlock, true, _blockSign);
        }
        // tail literal
        String lastLiteralBlock = _text.substring(_offset);
        _offset = _text.length();
        return new ScriptBlock(lastLiteralBlock, true, _blockSign);
    }

    protected int getOffset() {
        return _offset;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

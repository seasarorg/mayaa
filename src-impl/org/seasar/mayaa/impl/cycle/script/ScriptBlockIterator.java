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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.mayaa.impl.util.StringUtil;

/**
 * テキストからスクリプトを順番に読み出すIterator。
 * スクリプトブロック(デフォルトでは${})は実行可能なスクリプトとして、
 * それ以外は静的なテキストとして扱います。
 * "${aaa}bbb${ccc}${ddd}eee" というテキストの場合、next()を呼ぶたびに
 * 実行可能なaaa、静的なbbb、実行可能なccc、実行可能なddd、静的なeeeの
 * 順にScriptBlockを返します。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockIterator implements Iterator {

    private String _text;
    private String _blockSign;
    private int _offset;
    private boolean _onTemplate = true;

    /**
     * スクリプトを含むテキスト、スクリプトブロック開始記号、テンプレート上か
     * どうかを引数にとるコンストラクタ。
     * スクリプトブロック開始記号が"$"の場合、スクリプトブロックは"${}"になります。
     * また、テキストがテンプレート上にある場合、スクリプトブロック内の
     * エンティティ参照は解決後に処理されます。(&amp;lt;は&lt;として処理されます)
     *
     * @param text 元となるテキスト
     * @param blockSign スクリプトブロック開始記号
     * @param onTemplate テキストはテンプレート上のものか
     */
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

    /**
     * 次のスクリプトブロックを取得します。
     * 戻り値のScriptBlockは静的なテキストならScriptBlock#isLiteral()がtrueを
     * 返します。
     *
     * @return ScriptBlockインスタンス
     * @throws NoSuchElementException 次の要素が存在しない場合
     */
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
                    // テンプレート上の場合はエンティティ解決をしてからスクリプトに渡す
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

    /**
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

}

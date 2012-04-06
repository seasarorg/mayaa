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
package org.seasar.mayaa.impl.engine;

import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * テンプレート上のmetaタグでのcharset表記、およびHTTPレスポンスのcharset表記を
 * テンプレートで指定したcharset、およびmayaaファイルのm:contentTypeで指定した
 * charsetから異なるcharset表記にするためのクラス。
 *
 * 用途はWindows-31JとShift_JISの組み合わせ以外ないと想定しているため、現在は
 * 最低限の実装。もし汎用性が必要ならばこのクラスをAPIに昇格し、ServiceProvider
 * で取得できるようにする。
 *
 * @author Koji Suga (Gluegent Inc.)
 * @version 1.1.12
 */
public class CharsetConverter {

    /** 変換が有効か */
    private static volatile boolean _enabled = false;

    /**
     * 変換の有効/無効を設定します。
     *
     * @param enabled 有効にするならtrue
     */
    public static void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /**
     * contentType中のcharsetの値を探し、前後のindexを返します。
     * 戻り値をindexとすると、contentType.substring(index[0], index[1])で
     * charsetの値 (UTF-8など) を取り出せます。
     * 見つからない場合はnullを返します。
     *
     * @param contentType charsetを探すcontent-type
     * @return charsetの前後のindex。見つからない場合はnull
     */
    private static int[] getCharsetIndex(String contentType) {
        if (StringUtil.hasValue(contentType)) {
            String lower = contentType.toLowerCase();
            int startIndex = lower.indexOf("charset");
            if (startIndex > 0) {
                startIndex += 7; /*7 = "charset".length()*/
                final int eqIndex = contentType.indexOf("=", startIndex);
                if (eqIndex > 0) {
                    int endIndex = contentType.indexOf(";", eqIndex);
                    if (endIndex < 0) {
                        endIndex = contentType.length();
                    }
                    return new int[] { eqIndex + 1, endIndex };
                }
            }
        }
        return null;
    }

    /**
     * contentTypeからcharsetを抽出して返します。
     * charsetが存在しない場合はデフォルトのUTF-8を返します。
     * また、必要ならばcharsetをencoding用に変換して返します。
     *
     * @param contentType charset表記を含むcontentType
     * @return 抽出したcharsetをencodingとして使うための名前。
     */
    public static String extractEncoding(String contentType) {
        int[] index = getCharsetIndex(contentType);
        if (index != null) {
            String charset = contentType.substring(index[0], index[1]).trim();
            return charsetToEncoding(charset);
        }
        return CONST_IMPL.TEMPLATE_DEFAULT_CHARSET;
    }

    /**
     * contentTypeのcharsetをencodingと見なし、charsetに変換したものを返します。
     * また、charsetを含まない場合はutf-8のcharsetを付与して返します。
     * 特に変換する必要がない場合は引数をそのまま返します。
     *
     * @param contentType 変換するcharset表記を含むcontentType
     * @return 変換後のcontentType。
     */
    public static String convertContentType(String contentType) {
        int[] index = getCharsetIndex(contentType);
        if (index == null) {
            if (StringUtil.hasValue(contentType)) {
                if (contentType.charAt(contentType.length() - 1) == ';') {
                    return contentType + " charset=UTF-8";
                }
                return contentType + "; charset=UTF-8";
            }
            return contentType;
        }
        if (_enabled) {
            String charset = contentType.substring(index[0], index[1]).trim();
            return contentType.substring(0, index[0]) +
                    encodingToCharset(charset) + contentType.substring(index[1]);
        }
        return contentType;
    }

    /**
     * charset表記に対応するencoding名を取得します。
     * 特に変換する必要がない場合は引数をそのまま返します。
     *
     * @param charset 変換するcharset表記
     * @return 引数に対応するencoding名、または引数そのもの。
     */
    public static String charsetToEncoding(String charset) {
        if (_enabled && "Shift_JIS".equalsIgnoreCase(charset)) {
            return "Windows-31J";
        }
        return charset;
    }

    /**
     * encoding名に対応するcharset表記を取得します。
     * 特に変換する必要がない場合は引数をそのまま返します。
     *
     * @param encoding 変換するencoding名
     * @return 引数に対応するcharset表記、または引数そのもの。
     */
    public static String encodingToCharset(String encoding) {
        if (_enabled && "Windows-31J".equalsIgnoreCase(encoding)) {
            return "Shift_JIS";
        }
        return encoding;
    }

}

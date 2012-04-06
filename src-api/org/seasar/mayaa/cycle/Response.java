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
package org.seasar.mayaa.cycle;

import java.io.OutputStream;
import java.io.Serializable;

import org.seasar.mayaa.ContextAware;


/**
 * レスポンスのインターフェイス。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Response extends Serializable, ContextAware  {

    /**
     * コンテンツタイプの指定を行う。
     * @param contentType コンテンツタイプ。MIME型およびエンコーディング情報。
     */
    void setContentType(String contentType);

    /**
     * HTTPステータスコードの設定。
     * @param code ステータスコード。通常は200。404、500など。
     */
    void setStatus(int code);

    /**
     * レスポンスヘッダの書き出し。
     * @param name ヘッダ名。
     * @param value ヘッダの値。
     */
    void addHeader(String name, String value);

    /**
     * レスポンスヘッダの書き出しまたは上書き。
     * @param name ヘッダ名。
     * @param value ヘッダの値。
     */
    void setHeader(String name, String value);

    /**
     * 書き出しバッファのクリアを行う。
     */
    void clearBuffer();

    /**
     * バッファへの書き出し。
     * @param b 書き出し値。
     */
    void write(int b);

    /**
     * バッファへの書き出し。
     * @param cbuf 書き出し値。
     */
    void write(char[] cbuf);

    /**
     * バッファへの書き出し。
     * @param cbuf 書き出し値。
     * @param off cbufの書き出しオフセット。
     * @param len cbufの書き出し長。
     */
    void write(char[] cbuf, int off, int len);

    /**
     * バッファへの書き出し。
     * @param str 書き出し値。
     */
    void write(String str);

    /**
     * バッファへの書き出し。
     * @param str 書き出し値。
     * @param off strの書き出しオフセット。
     * @param len strの書き出し長。
     */
    void write(String str, int off, int len);

    /**
     * バッファのフラッシュ。カレントバッファがスタックに積まれている場合は、
     * ひとつ上位のバッファに書き出す。ルートのバッファである場合には、
     * 実際の出力ストリームに書き出す。
     */
    void flush();

    /**
     * バッファされたライタを取得する。
     * @return バッファされたライタ。
     */
    CycleWriter getWriter();

    /**
     * バッファされたライタをスタックに積む。
     * @return 積んだバッファされたライタ。
     */
    CycleWriter pushWriter();

    /**
     * バッファされたライタをスタックに積む。内容を取得するためのライタなのでflushを呼ばれてもflushしない。
     * @return 積んだバッファされたライタ。
     */
    CycleWriter pushNoFlushWriter();

    /**
     * スタック最上位のバッファライタを取り除く。
     * @return 取り除いたバッファライタ。
     */
    CycleWriter popWriter();

    /**
     * 実際の出力ストリームの取得。
     * @return 出力ストリーム。
     */
    OutputStream getOutputStream();

    /**
     * 渡されたURL文字列に、必要であればセッションIDを付加する。
     * @param url URL文字列。
     * @return セッションIDを付加した文字列。
     */
    String encodeURL(String url);

}

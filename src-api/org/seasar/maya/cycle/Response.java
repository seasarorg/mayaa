/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.cycle;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * レスポンスのインターフェイス。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Response extends Serializable, Underlyable  {
    
    /**
     * コンテンツタイプの指定を行う。
     * @param mimeType コンテンツタイプ。MIME型およびエンコーディング情報。
     */
    void setMimeType(String mimeType);
    
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
     * 書き出しバッファのクリアを行う。
     */
    void clearBuffer();
    
    /**
     * 書き出しバッファの内容を取得する。
     * @return バッファ内容。
     */
    String getBuffer();
    
    /**
     * バッファへの書き出し。
     * @param b 書き出し値。
     */
    void write(int b);
    
    /**
     * バッファへの書き出し。
     * @param cbuf 書き出し値。
     */
    void write(char cbuf[]);
    
    /**
     * バッファへの書き出し。
     * @param cbuf 書き出し値。
     * @param off cbufの書き出しオフセット。
     * @param len cbufの書き出し長。
     */
    void write(char cbuf[], int off, int len);
    
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
     * スタック最上位のバッファライタを取り除く。
     * @return 取り除いたバッファライタ。
     */
    CycleWriter popWriter();
    
    /**
     * 実際の出力ストリームの取得。
     * @return 出力ストリーム。
     */
    OutputStream getUnderlyingOutputStream();
    
}

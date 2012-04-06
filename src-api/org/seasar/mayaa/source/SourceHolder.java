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
package org.seasar.mayaa.source;

import java.util.Iterator;

import org.seasar.mayaa.source.SourceDescriptor;

/**
 * ソースディスクリプタを格納しているロケーション
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface SourceHolder {

    /**
     * systemIDのイテレータを返却する。
     * 階層構造もトレースし、/ で区切られたものを返却する。
     *
     * @param filters ピリオドと小文字の1つ以上の連続の場合は拡張子を、
     *                 それ以外は正規表現でファイル名のマッチしたものを返却する。
     * @return 存在するsystemIDイテレータ
     */
    Iterator iterator(String[] filters);

    /**
     * ソースディスクリプタを保持する論理的な開始位置を設定する
     * @param root 格納開始位置
     */
    void setRoot(String root);

    /**
     * ソースディスクリプタを保持する
     * 論理的な開始位置を返却する
     * @return 格納開始位置
     */
    String getRoot();

    /**
     * ソースディスクリプタを生成する
     *
     * @param systemID
     *            システムID
     * @return ソースディスクリプタ
     */
    SourceDescriptor getSourceDescriptor(String systemID);

}

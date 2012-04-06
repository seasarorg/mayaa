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
package org.seasar.mayaa.engine.specification;

import java.util.Date;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * スペック情報にアクセスするためのインターフェイス
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Specification
        extends NodeTreeWalker, SequenceIDGenerator, ParameterAware {

    /**
     * 最終ビルド時を取得する。
     * @return ビルド時。未ビルドの場合nullを返す。
     */
    Date getTimestamp();

    /**
     * 設定XMLのソース設定。
     * @param source 設定XMLソース。
     */
    void setSource(SourceDescriptor source);

    /**
     * 設定XMLのソースを取得する。
     * @return 設定XMLソース。
     */
    SourceDescriptor getSource();

    /**
     * スペック情報が廃止対象としてマークされているかどうかを返す。
     * 古いソースでビルドされている場合と、未使用期間が一定の長さを
     * 超えた場合に真となる。
     * この値が真の時は、ノード構成やノード内容は保証されない。
     * @return 廃止対象としてマークされているなら{@code true}
     */
    boolean isDeprecated();

    /**
     * ソースビルドを行う。
     * 常に再ビルドとして動作する。
     */
    void build();

    /**
     * 再ビルドかどうかを指定してソースビルドを行う。
     * 再ビルドの場合、ソースが存在しない場合も最新ビルド時を更新する。
     * 再ビルドでない場合、ソースが存在しなければ最新ビルド時は最小値となる。
     * @param rebuild 再ビルドか否か。再ビルドなら{@code true}、そうでなければ{@code false}
     */
    void build(boolean rebuild);

}

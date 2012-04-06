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
package org.seasar.mayaa.engine.processor;

import java.io.Serializable;
import java.util.Map;

import org.seasar.mayaa.engine.specification.serialize.NodeReferenceResolverFinder;
import org.seasar.mayaa.engine.specification.serialize.ProcessorReferenceResolverFinder;

/**
 * プロセッサツリーを操作する。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorTreeWalker
        extends ProcessorReferenceResolverFinder,
                 NodeReferenceResolverFinder, Serializable {

    /**
     * プロセッサ実行スコープに、登録スクリプト変数を提供する。
     * @return 登録変数のマップ。キーが変数名となる。
     */
    Map getVariables();

    /**
     * ノードの初期化を行う。このメソッドは、TemplateBuilder#buildの中で呼ばれる。
     * @param parent 親ProcessorTreeWalker
     */
    void setParentProcessor(ProcessorTreeWalker parent);

    /**
     * 子ProcessorTreeWalkerを追加する。このメソッドは、
     * TemplateBuilder#buildの中で呼ばれる。
     * @param child 子ProcessorTreeWalker
     */
    void addChildProcessor(ProcessorTreeWalker child);

    /**
     * 子ProcessorTreeWalkerを指定した位置に挿入して追加する。
     * @param index インデックス値
     * @param child 子ProcessorTreeWalker
     */
    void insertProcessor(int index, ProcessorTreeWalker child);

    /**
     * 子ProcessorTreeWalkerを削除する。
     * @param child 子ProcessorTreeWalker
     * @return 削除した場合はtrue。存在しなかった場合はfalseを返す。
     */
    boolean removeProcessor(ProcessorTreeWalker child);

    /**
     * 親ProcessorTreeWalkerを取得する。
     * @return 親ProcessorTreeWalker
     */
    ProcessorTreeWalker getParentProcessor();

    /**
     * 静的な親ProcessorTreeWalkerを取得する。
     * 基本的には{@link #getParentProcessor()}と同じ結果になるが、
     * 動的な親の変更を考慮せず、静的な位置関係の親を取得する。
     * @return 親ProcessorTreeWalker
     * @since 1.1.26
     */
    ProcessorTreeWalker getStaticParentProcessor();

    /**
     * 子ProcessorTreeWalkerの数を取得する。
     * @return 子ProcessorTreeWalkerの数
     */
    int getChildProcessorSize();

    /**
     * 指定インデックスの子ProcessorTreeWalkerを取得する。
     * @param index 指定index。
     * @return 指定indexの子ProcessorTreeWalker。
     */
    ProcessorTreeWalker getChildProcessor(int index);

    /**
     * 子ProcessorTreeWalkerを全て削除する。
     */
    void clearChildProcessors();

}

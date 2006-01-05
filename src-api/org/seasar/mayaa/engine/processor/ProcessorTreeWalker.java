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
package org.seasar.mayaa.engine.processor;

import java.io.Serializable;
import java.util.Map;

/**
 * プロセッサツリーを操作する。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ProcessorTreeWalker extends Serializable {

    /**
     * プロセッサ実行スコープに、登録スクリプト変数を提供する。
     * @return 登録変数のマップ。キーが変数名となる。
     */
    Map getVariables();

    /**
     * ノードの初期化を行う。このメソッドは、TemplateBuilder#buildの中で呼ばれる。
     * @param parent 親ProcessorTreeWalker
     * @param index 親ProcessorTreeWalker内での子としてのインデックス値。
     */
    void setParentProcessor(ProcessorTreeWalker parent, int index);

    /**
     * 子ProcessorTreeWalkerを追加する。このメソッドは、
     * TemplateBuilder#buildの中で呼ばれる。
     * @param child 子ProcessorTreeWalker
     */
    void addChildProcessor(ProcessorTreeWalker child);

    /**
     * 親ProcessorTreeWalker内での子としてのインデックス値
     * @return インデックス値
     */
    int getIndex();

    /**
     * 親ProcessorTreeWalkerを取得する。
     * @return 親ProcessorTreeWalker
     */
    ProcessorTreeWalker getParentProcessor();

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

}

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
package org.seasar.maya.engine.processor;

import org.seasar.maya.cycle.CycleWriter;

/**
 * TemplateProcessorの拡張インターフェイス。子要素の評価の機能を持つ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ChildEvaluationProcessor extends IterationProcessor {

    /**
     * リターンフラグ。この値をdoStartProcess()が返すと、プロセッサボディをバッファリングする。
     */
    ProcessStatus EVAL_BODY_BUFFERED = new ProcessStatus();
    
    /**
     * ボディのスタック評価を行うかを返す。JSPのBodyTagをホストしている場合に
     * 利用する。デフォルトではfalseを返す。trueだと、setBodyContent()メソッド
     * およびdoInitChildProcess()メソッドがコンテナより呼び出される。
     * @return ボディのスタック評価をする場合、true。普通はfalse。
     */
    boolean isChildEvaluation();
    
    /**
     * ボディのスタック評価を行う場合、スタック処理が行われたボディ部のバッファを
     * コンテナがセットする。
     * @param body スタックに積まれたボディ部のバッファ。
     */
    void setBodyContent(CycleWriter body);

    /**
     * ボディのスタック評価を行う場合、評価前に一度、コンテナより呼び出される。
     */
    void doInitChildProcess();

}
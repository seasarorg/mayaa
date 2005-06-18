/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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

import javax.servlet.jsp.PageContext;

/**
 * TemplateProcessorの拡張インターフェイス。例外処理関連のイベントを
 * 受け取る機能を持たせる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface TryCatchFinallyProcessor extends TemplateProcessor {

    /**
     * 例外をcatchするかどうかを返す。JSPのTryCatchFinallyをホストしている場合に
     * 利用する。デフォルトではfalseを返す。trueだと、例外発生時に
     * doCatchProcess、例外とは無関係にdoFinallyProcessがコンテナより呼び出される。
     * @param context プロセス中のステートフルな情報を保持するコンテキスト。
     * @return 例外をcatchする場合、true。普通はfalse。
     */
    boolean canCatch(PageContext context);

    /**
     * プロセス中の例外をキャッチして行う処理。
     * @param context プロセス中のステートフルな情報を保持するコンテキスト。
     * @param t プロセス中に発生した例外
     */
    void doCatchProcess(PageContext context, Throwable t);

    /**
     * プロセス中に例外が起きても行う後処理。
     * @param context プロセス中のステートフルな情報を保持するコンテキスト。
     */
    void doFinallyProcess(PageContext context);

}
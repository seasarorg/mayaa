/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.engine;

import org.seasar.maya.ParameterAware;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.engine.specification.Specification;

/**
 * ランタイムエンジン。ホストサーブレットからservice()が呼び出される。 
 * リクエストに対して、ステートレスに実装。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Engine extends ParameterAware, Specification {
    
    /**
     * エラーハンドラの設定。
     * @param errorHandler エラーハンドラ。
     */
    void setErrorHandler(ErrorHandler errorHandler);
    
    /**
     * エラーハンドラの取得。
     * @return エラーハンドラ。
     */
    ErrorHandler getErrorHandler();
    
	/**
	 * Pageオブジェクトのインスタンスを返す。
     * @param pageName ページ名。
	 * @return Pageオブジェクト。
	 */
    Page getPage(String pageName);

    /**
     * サービスメソッド。
     * @param pageFlush テンプレート出力を自動でフラッシュするかどうか。
     */
    void doService(boolean pageFlush);

    /**
     * エラーハンドルページの表示。
     * @param t 発生した例外。
     * @param pageFlush テンプレート出力を自動でフラッシュするかどうか。
     */
    void handleError(Throwable t, boolean pageFlush);
    
    /**
     * プロセッサツリーのデコードフェーズを起動するかどうかを設定する。
     * @param processDecode tureだとデコードフェーズ起動。
     */
    void setProcessDecode(boolean processDecode);
    
    /**
     * プロセッサツリーのデコードフェーズを起動するかどうかを取得する。
     * @return tureだとデコードフェーズ起動。デフォルトfalse。
     */
    boolean isProcessDecode();
    
}
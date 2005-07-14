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
package org.seasar.maya.engine;

import java.io.Serializable;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.provider.EngineSetting;
import org.seasar.maya.provider.Parameterizable;

/**
 * TODO ServiceCycle
 * 
 * ランタイムエンジン。ホストサーブレットからservice()が呼び出される。 
 * リクエストに対して、ステートレスに実装。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Engine extends Parameterizable, Specification, Serializable {
    
    /**
     * エラーハンドラの取得。
     * @return エラーハンドラ。
     */
    ErrorHandler getErrorHandler();
    
    /**
     * 設定の取得。
     * @return 設定。
     */
    EngineSetting getEngineSetting();    

    /**
     * ユーザーリクエストが「/」で終わるディレクトリアクセスの際に
     * 補完するウェルカムファイル名を取得する。
     * @return ウェルカムファイル名。
     */
    String getWelcomeFileName();
    
	/**
	 * Pageオブジェクトのインスタンスを返す。
     * @param pageName ページ名。
     * @param extension ページ拡張子。
	 * @return Pageオブジェクト。
	 */
    Page getPage(String pageName, String extension);

    /**
     * フォワードを行う際のサービスメソッド。
     * @param context 既存コンテキスト。
     * @param pageName ページ名。「/WEB-INF」フォルダを含むことができる。
     * @param requestedSuffix リクエストで強制するページ接尾辞。nullでもよい。
     * @param extension ページ拡張子。
     */
    void doService(PageContext context, String pageName, 
    		String requestedSuffix, String extension);
    
}
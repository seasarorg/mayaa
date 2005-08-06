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
package org.seasar.maya.provider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.el.ExpressionFactory;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * アプリケーションスコープでのサービス提供オブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ServiceProvider {

    /**
     * アプリケーションコンテキストの取得。
     * @return アプリケーションコンテキスト。
     */
    Application getApplication();
    
	/**
	 * ソースディスクリプタのファクトリ取得。
	 * @return ソースディスクリプタのファクトリオブジェクト。
	 */
	SourceFactory getSourceFactory(); 

    /**
     * エンジンの生成を行う。
     * @return	エンジン
     */
    Engine getEngine();
    
    /**
     * 式評価エンジンの取得
     * @return 式評価エンジン。
     */
    ExpressionFactory getExpressionFactory();
    
    /**
     * 設定XMLのビルダを取得する。
     * @return 設定XMLビルダ。
     */
    SpecificationBuilder getSpecificationBuilder();
	
    /**
     * HTMLテンプレートファイルのビルダを取得する。
     * @return テンプレートビルダ。
     */
    TemplateBuilder getTemplateBuilder();

    /**
     * サーブレットAPIのコンテキストオブジェクト設定。
     * @param request カレントのHTTPリクエスト。
     */
    void setHttpServletRequest(HttpServletRequest request);
    
    /**
     * サーブレットAPIのコンテキストオブジェクト設定。
     * @param request カレントのHTTPレスポンス。
     */
    void setHttpServletResponse(HttpServletResponse response);
    
    /**
     * サービスサイクルの取得
     * @return 払い出したサービスサイクル。
     */
    ServiceCycle getServiceCycle();

    /**
     * サービスサイクルの返却
     * @param cycle 返却するサービスサイクル。
     */
    void releaseServiceCycle(ServiceCycle cycle);

}

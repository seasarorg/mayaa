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

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.ScriptEnvironment;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.source.SourceDescriptor;

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
     * エンジンの生成を行う。
     * @return	エンジン
     */
    Engine getEngine();
    
    /**
     * スクリプト実行環境の取得。
     * @return スクリプト実行環境。
     */
    ScriptEnvironment getScriptEnvironment();
    
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
     * テンプレートや設定XMLを読み出すソースを生成する。
     * @param systemID ソースのSystemID。
     * @return 指定SystemIDのソース。
     */
    SourceDescriptor getPageSourceDescriptor(String systemID);
    
    /**
     * リクエストおよびレスポンスのコンテキストオブジェクト設定。
     * @param request カレントのリクエストオブジェクト。
     * @param response カレントのレスポンスオブジェクト。
     */
    void initialize(Object request, Object response);
    
    /**
     * サービスサイクルの取得
     * @return カレントスレッドでのサービスサイクル。
     */
    ServiceCycle getServiceCycle();

    /**
     * ユーザー定義のモデルオブジェクト取得メソッド。
     * @param modelKey コンポーネントキー。
     * @param modelScope スコープ。
     * @return モデルオブジェクト。
     */
    Object getModel(Object modelKey, String modelScope);
    
}

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

import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;

/**
 * テンプレートを描画するオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface TemplateRenderer {
	
	/**
     * 指定テンプレートを描画する。
     * @param topLevelPage  描画トップレベルのページ。
     * @param template 描画するテンプレート。
     * @return テンプレートプロセッサのプロセスステートフラグ。
     */
    ProcessStatus renderTemplate(Page topLevelPage, Template template);

	/**
	 * 指定テンプレートをデコードする。
     * @param topLevelPage  描画トップレベルのページ。
     * @param template 描画するテンプレート。
	 */
    void decodeTemplate(Page topLevelPage, Template template);
    
}

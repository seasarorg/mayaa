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
package org.seasar.maya.engine;

import java.io.Serializable;

import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.Specification;

/**
 * テンプレートのレンダリングエントリーポイント。アプリケーションを構成する各ページのモデル
 * であり、リクエストに対してはステートレスなオブジェクトである。Engine#doService(PageContext)
 * で、テンプレートを初期化をした後、doTemplate(ProcessorContext)が呼ばれる。また、
 * TemplateCustomTag#doStartTag()でdoTemplate(ProcessorContext)が呼ばれる。
 * シリアライズ可能。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Page extends Specification, Serializable {

	/**
	 * エンジンへの参照を取得。
	 * @return エンジン。
	 */
	Engine getEngine();
	
    /**
     * ページの名前を取得する。/context/hello.htmlであれば、「/context/hello」を返す。
     * @return ページ名。
     */
    String getPageName();

    /**
     * ページの拡張子を返す。/context/hello.htmlだと、「html」。ドットを含まない。
     * @return ページの拡張子。
     */
    String getExtension();

    /**
     * テンプレート接尾辞より適切なTemplateオブジェクトをロードして返す。
     * @param suffix ページ接尾辞。nullでもよい。
     * @return レンダリングするテンプレート。
     */
    Template getTemplate(String suffix);
	
    /**
     * テンプレートレンダリングを行う。
     * @return プロセッサ処理ステータス。
     */
    ProcessStatus doPageRender();

}

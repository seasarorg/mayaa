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

import org.seasar.maya.engine.processor.ProcessorTreeWalker;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.engine.specification.Specification;

/**
 * テンプレートオブジェクト。HTMLをパースした結果の、
 * TemplateProcessorのツリー構造を内包。
 * リクエストに対して、ステートレスである。 シリアライズ可能。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface Template 
        extends Specification, ProcessorTreeWalker, Serializable {
    
	/**
	 * ページへの参照を取得する。
	 * @return ページ。
	 */
	Page getPage();
	
    /**
     * テンプレートの接尾子を返す。hello_ja.htmlであれば、「ja」を返す。
     * hello.htmlでは空白文字列。
     * @return テンプレートの接尾子。
     */
    String getSuffix();

    /**
     * ページの拡張子を返す。/context/hello.htmlだと、「html」。ドットを含まない。
     * @return ページの拡張子。
     */
    String getExtension();

    /**
     * テンプレートをレンダリングする。
     * @return テンプレートプロセッサのプロセスステートフラグ。
     */
    ProcessStatus doTemplateRender();
    
}

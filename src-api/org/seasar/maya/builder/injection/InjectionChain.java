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
package org.seasar.maya.builder.injection;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * テンプレートに記述されたHTMLタグに、追加的な情報を保持するノードを
 * インジェクションするレゾルバチェーン。このインターフェイスの実装オブジェクトは
 * エンジンが提供する。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface InjectionChain {

    /**
     * テンプレート上のオリジナルなノードにインジェクションするノードを決定する際の
     * チェーンメソッド。
     * @param template テンプレート。
     * @param original テンプレート上のオリジナルなノード。
     * @return インジェクションするノードもしくはnull。
     */
    SpecificationNode getNode(Template template, SpecificationNode original);
    
}

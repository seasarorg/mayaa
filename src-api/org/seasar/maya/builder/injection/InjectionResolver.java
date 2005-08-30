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
import org.seasar.maya.provider.Parameterizable;

/**
 * テンプレートに記述されたHTMLタグに、追加的な情報を保持するノードを
 * インジェクションするレゾルバ。このインターフェイスを実装してエンジンの挙動
 * をカスタマイズすることができる。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface InjectionResolver extends Parameterizable {

    /**
     * テンプレート上のオリジナルなノードにインジェクションするノードを決定する。
     * @param template テンプレート。
     * @param original テンプレート上のオリジナルなノード。
     * @param chain 次のリゾルバへ処理を委譲するチェーン。
     * @return インジェクションするノード。
     */
    SpecificationNode getNode(
            Template template, SpecificationNode original, InjectionChain chain);
    
}

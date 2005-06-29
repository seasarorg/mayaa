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
package org.seasar.maya.impl.builder;

import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.xml.sax.Locator;

/**
 * プレフィックスから名前空間URIを引けなかったとき。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PrefixMappingNotFoundException extends SpecificationBuildException {

	private static final long serialVersionUID = -9114023056056051237L;

	private String _prefix;
    
    public PrefixMappingNotFoundException(
    		Specification specification, SpecificationNode node, String prefix) {
    	super(specification, node);
    	_prefix = prefix;
    }
    
    
    /**
	 * @param specification テンプレートファイルもしくは、埋め込みページ、設定XML。
	 * @param locator 例外発生箇所。
     * @param namespaceURI マッピング対象が見つからなかったプレフィックス。
     */
    public PrefixMappingNotFoundException(
            Specification specification, Locator locator, String prefix) {
        super(specification, locator);
    	_prefix = prefix;
    }
    
    /**
     * 不正だったプレフィックスを取得する。
     * @return 不正なプレフィックス。
     */
    public String getPrefix() {
        return _prefix;
    }
    
}

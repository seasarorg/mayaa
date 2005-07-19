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
package org.seasar.maya.impl.builder;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * テンプレートや設定XMLに指定された属性値が不正な場合の例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IllegalAttributeValueException extends NodeNotResolvedException {

	private static final long serialVersionUID = -530046431089771029L;

	private String _attributeName;
	private String _attributeValue;
	
	/**
	 * @param template テンプレートファイル名もしくは、埋め込みページ名。
	 * @param node 例外発生箇所ノード。
	 * @param attributeName 不正な値がセットされた属性名。 
	 * @param attributeValue 不正な属性値。
	 */
	public IllegalAttributeValueException(Template template, 
			SpecificationNode node, String attributeName, String attributeValue) {
	    super(template, node);
		_attributeName = attributeName;
		_attributeValue = attributeValue;
    }
	
	/**
	 * 属性名を取得する。
	 * @return 不正な値の属性名。
	 */
	public String getAttributeName() {
		return _attributeName;
	}
	
	/**
	 * 属性値を取得する。
	 * @return 不正な属性値。
	 */
	public String getAttributeValue() {
	    if(_attributeValue == null) {
	        return "(null)";
	    }
		return _attributeValue;
	}
	
}

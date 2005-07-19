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
import org.seasar.maya.impl.MayaException;

/**
 * テンプレート上に記述したノードが解決されなかったときの例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NodeNotResolvedException extends MayaException {

	private static final long serialVersionUID = 4560512867759109674L;

	private Template _template;
	private SpecificationNode _specificationNode;
	
	/**
	 * @param template テンプレートファイルもしくは、埋め込みページ。
	 * @param locator 例外発生箇所。
	 * @param specificationNode 解決できなかったノード。
	 */
	public NodeNotResolvedException(
	        Template template, SpecificationNode specificationNode) {
	    _template = template;
	    _specificationNode = specificationNode;
    }

	/**
	 * 解決されなかったノードの所属するテンプレートを取得。
	 * @return 解決されなかったノードの所属するテンプレート。
	 */
	public Template getTemplete() {
	    return _template;
	}
	
	/**
	 * 解決されなかったノードを取得する。
	 * @return 解決されなかったノード。
	 */
	public SpecificationNode getSpecificationNode() {
		return _specificationNode;
	}

	/**
	 * 例外発生列の取得。
	 * @return 例外発生列もしくは0。
	 */
	public int getColumnNumber() {
		return _specificationNode.getLocator().getColumnNumber();
	}
	
	/**
	 * 例外発生行の取得。
	 * @return 例外発生行もしくは0。
	 */
	public int getLineNumber() {
		return _specificationNode.getLocator().getLineNumber();
	}
	
	/**
	 * ソースのPublicIDの取得。
	 * @return ソースのPublicIDもしくはnull。
	 */
	public String getPublicID() {
		return _specificationNode.getLocator().getPublicId();
	}
	
	/**
	 * ソースのSystemIDの取得。
	 * @return ソースのSystemIDもしくはnull。
	 */
	public String getSystemID() {
		return _specificationNode.getLocator().getSystemId();
	}
	
}

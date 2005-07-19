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

import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.MayaException;
import org.xml.sax.Locator;

/**
 * テンプレートビルド時の基本的な例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationBuildException extends MayaException {
    
	private static final long serialVersionUID = -5042681474924383369L;

	private Specification _specification;
	private Locator _locator;
	
	/**
	 * @param specification ビルドを失敗したテンプレート/設定XML。
	 * @param node 例外に関連するノード。
	 */
	protected SpecificationBuildException(Specification specification, SpecificationNode node) {
		_specification = specification;
		if(node != null) {
			_locator = node.getLocator();
		}
	}
	
	/**
	 * @param specification ビルドを失敗したテンプレート/設定XML。
	 * @param locator 例外発生箇所。
	 */
	protected SpecificationBuildException(Specification specification, Locator locator) {
		_specification = specification;
		_locator = locator;
    }
	
	/**
	 * ビルドを失敗したテンプレート/設定XMLを取得する。
	 * @return ビルドを失敗したテンプレート/設定XML。
	 */
	public Specification getSpecification() {
		return _specification;
	}

	/**
	 * 例外発生列の取得。
	 * @return 例外発生列もしくは0。
	 */
	public int getColumnNumber() {
		return _locator.getColumnNumber();
	}
	
	/**
	 * 例外発生行の取得。
	 * @return 例外発生行もしくは0。
	 */
	public int getLineNumber() {
		return _locator.getLineNumber();
	}
	
	/**
	 * ソースのPublicIDの取得。
	 * @return ソースのPublicIDもしくはnull。
	 */
	public String getPublicID() {
		return _locator.getPublicId();
	}
	
	/**
	 * ソースのSystemIDの取得。
	 * @return ソースのSystemIDもしくはnull。
	 */
	public String getSystemID() {
		return _locator.getSystemId();
	}
	
}

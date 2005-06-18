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
import org.xml.sax.Locator;

/**
 * テンプレートや設定XMLに指定されたノード名や属性名が不正な場合の例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IllegalNameException extends SpecificationBuildException {

	private String _qName;
	
	/**
	 * @param specification テンプレートファイル名もしくは、埋め込みページ名。
	 * @param locator 例外発生箇所。
	 * @param qName 不正なノード名。 
	 */
	public IllegalNameException(
	        Specification specification, Locator locator, String qName) {
	    super(specification, locator);
		_qName = qName;
    }
	
	/**
	 * ノード名を取得する。
	 * @return 不正なノード名。
	 */
	public String getQName() {
		return _qName;
	}
	
}

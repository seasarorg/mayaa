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

import org.seasar.maya.impl.MayaException;

/**
 * テンプレートや設定XMLに指定されたノード名や属性名が不正な場合の例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class IllegalNameException extends MayaException {

	private static final long serialVersionUID = 2431120366863355234L;

	private String _qName;
	
	/**
	 * @param qName 不正なノード名。 
	 */
	public IllegalNameException(String qName) {
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

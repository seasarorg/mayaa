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

import org.seasar.maya.impl.MayaException;

/**
 * 指定URLに対するページやテンプレートが見つからないときの例外。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageNotFoundException extends MayaException {

	private static final long serialVersionUID = 3750171533372115950L;

	private String _pageName;
    
    /**
	 * @param pageName ページ名もしくは、埋め込みページ名。
	 */
	public PageNotFoundException(String pageName) {
	    _pageName = pageName;
    }

	/**
	 * ページ名を取得する。
	 * @return 問題のあるページの名前。
	 */
	public String getPageName() {
		return _pageName;
	}
	
}

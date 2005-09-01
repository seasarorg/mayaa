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
package org.seasar.maya.impl.cycle.web;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class ParamScope extends ParamValuesScope {
	
	public ParamScope(HttpServletRequest request) {
		super(request);
	}

	public String getScopeName() {
		return "param";
	}

	public Object getAttribute(String name) {
		String[] ret = (String[])super.getAttribute(name);
		if(ret != null && ret.length > 0) {
			return ret[0];
		}
		return null;
	}

}

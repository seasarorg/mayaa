/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.standard.util;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.CONST_IMPL;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JspUtil implements CONST_IMPL {
    
	private JspUtil() {
	}

    public static int getScopeFromString(String value) {
        if(ServiceCycle.SCOPE_APPLICATION.equalsIgnoreCase(value)) {
            return PageContext.APPLICATION_SCOPE;
        } else if(ServiceCycle.SCOPE_SESSION.equalsIgnoreCase(value)) {
            return PageContext.SESSION_SCOPE;
        } else if(ServiceCycle.SCOPE_REQUEST.equalsIgnoreCase(value)) {
            return PageContext.REQUEST_SCOPE;
        } else if(ServiceCycle.SCOPE_PAGE.equalsIgnoreCase(value)) {
            return PageContext.PAGE_SCOPE;
        }
        throw new IllegalArgumentException();
    }
    
    public static String getScopeFromInt(int scope) {
        if(scope == PageContext.APPLICATION_SCOPE) {
            return ServiceCycle.SCOPE_APPLICATION;
        } else if(scope == PageContext.SESSION_SCOPE) {
            return ServiceCycle.SCOPE_SESSION;
        } else if(scope == PageContext.REQUEST_SCOPE) {
            return ServiceCycle.SCOPE_REQUEST;
        } else if(scope == PageContext.PAGE_SCOPE) {
            return ServiceCycle.SCOPE_PAGE;
        }
        throw new IllegalArgumentException();
    }
	
}

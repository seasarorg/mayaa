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
package org.seasar.maya.standard;

import javax.servlet.jsp.PageContext;

import org.seasar.maya.cycle.ServiceCycle;

/**
 * 実装で必要な定数の定義。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_STANDARD {

    String LINE_SEPARATOR = System.getProperty("line.separator");
    
    int[] JSP_SCOPES = {
            PageContext.PAGE_SCOPE,
            PageContext.REQUEST_SCOPE,
            PageContext.SESSION_SCOPE,
            PageContext.APPLICATION_SCOPE
    }; 
    
    String[] CYCLE_SCOPES = {
            ServiceCycle.SCOPE_PAGE,
            ServiceCycle.SCOPE_REQUEST,
            ServiceCycle.SCOPE_SESSION,
            ServiceCycle.SCOPE_APPLICATION
    };
    
}

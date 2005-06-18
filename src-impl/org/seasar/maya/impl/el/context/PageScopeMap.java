/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.el.context;

import java.util.Enumeration;

import javax.servlet.jsp.PageContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageScopeMap extends AbstractAttributeMap {
    
    private PageContext _pageContext;

    public PageScopeMap(PageContext pageContext) {
        if(pageContext == null) {
            throw new IllegalArgumentException();
        }
        _pageContext = pageContext;
    }

    protected Object getAttribute(String key) {
        return _pageContext.getAttribute(key);
    }

    protected void setAttribute(String key, Object value) {
        _pageContext.setAttribute(key, value);
    }

    protected void removeAttribute(String key) {
        _pageContext.removeAttribute(key);
    }

    protected Enumeration getAttributeNames() {
        return _pageContext.getAttributeNamesInScope(PageContext.PAGE_SCOPE);
    }

}
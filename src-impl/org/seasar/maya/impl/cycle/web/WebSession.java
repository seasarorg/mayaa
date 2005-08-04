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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.EnumerationIterator;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebSession implements Session {

    private static final long serialVersionUID = -3211729351966533995L;

    private HttpServletRequest _httpServletRequest;
    private HttpSession _session;
    
    private void check() {
        if(_httpServletRequest == null) {
            throw new IllegalStateException();
        }
    }

    public Object getUnderlyingObject() {
        check();
        if(_session != null) {
            return _session;
        }
        return _httpServletRequest.getSession(true);
    }
    
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpServletRequest = httpServletRequest;
    }
    
    public String getID() {
        check();
        return _httpServletRequest.getSession(true).getId();
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_SESSION;
    }
    
    public Iterator iterateAttributeNames() {
        if(_session == null) {
            return NullIterator.getInstance();
        }
        return EnumerationIterator.getInstance(_session.getAttributeNames());
    }

    public Object getAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        if(_session != null) {
            return _session.getAttribute(name);
        }
        return null;
    }

    public void setAttribute(String name, Object attribute) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        if(_session == null) {
            _session = _httpServletRequest.getSession(true);
        }
        _session.setAttribute(name, attribute);
    }
    
    public void removeAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        if(_session != null) {
            _session.removeAttribute(name);
        }
    }

}

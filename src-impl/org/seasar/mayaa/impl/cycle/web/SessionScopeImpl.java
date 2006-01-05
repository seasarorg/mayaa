/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.cycle.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.SessionScope;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.cycle.scope.AbstractWritableAttributeScope;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SessionScopeImpl extends AbstractWritableAttributeScope
        implements SessionScope {

    private static final long serialVersionUID = -3211729351966533995L;

    private HttpServletRequest _httpRequest;
    private HttpSession _httpSession;

    protected void check() {
        if(_httpRequest == null) {
            throw new IllegalStateException();
        }
        if(_httpSession == null) {
            _httpSession = _httpRequest.getSession(true);
        }
    }

    // Session implements -------------------------------------------

    public String getSessionID() {
        check();
        return _httpSession.getId();
    }

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return ServiceCycle.SCOPE_SESSION;
    }

    public Iterator iterateAttributeNames() {
        check();
        return EnumerationIterator.getInstance(
                _httpSession.getAttributeNames());
    }

    public boolean hasAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return false;
        }
        check();
        for(Enumeration e = _httpSession.getAttributeNames();
                e.hasMoreElements(); ) {
            if(e.nextElement().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Object getAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        check();
        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        return env.convertFromScriptObject(_httpSession.getAttribute(name));
    }

    public void setAttribute(String name, Object attribute) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        check();
        _httpSession.setAttribute(name, attribute);
    }

    public void removeAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        check();
        _httpSession.removeAttribute(name);
    }

    // ContextAware implemetns ----------------------------------------

    public void setUnderlyingContext(Object context) {
        // When setting, UnderlyingObject is "HttpServletRequest"
        if(context == null ||
                context instanceof HttpServletRequest == false) {
            throw new IllegalArgumentException();
        }
        _httpRequest = (HttpServletRequest)context;
        _httpSession = null;
    }

    public Object getUnderlyingContext() {
        // When getting, UnderlyingObject is "HttpSession"
        check();
        return _httpSession;
    }

}

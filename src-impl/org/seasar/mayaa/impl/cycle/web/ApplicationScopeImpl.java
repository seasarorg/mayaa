/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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

import jakarta.servlet.ServletContext;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.impl.cycle.scope.AbstractWritableAttributeScope;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationScopeImpl
        extends AbstractWritableAttributeScope
        implements ApplicationScope {

    private static final long serialVersionUID = 5216746677132589700L;

    private transient ServletContext _servletContext;

    protected void check() {
        if (_servletContext == null) {
            throw new IllegalStateException();
        }
    }

    public String getMimeType(String fileName) {
        check();
        if (StringUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getMimeType(fileName);
    }

    public String getRealPath(String contextRelatedPath) {
        check();
        if (StringUtil.isEmpty(contextRelatedPath)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getRealPath(contextRelatedPath);
    }

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return ServiceCycle.SCOPE_APPLICATION;
    }

    public Iterator<String> iterateAttributeNames() {
        check();
        Enumeration<String> e = _servletContext.getAttributeNames();
        return EnumerationIterator.getInstance(e);
    }

    public boolean hasAttribute(String name) {
        check();
        if (StringUtil.isEmpty(name)) {
            return false;
        }

        Enumeration<String> e = _servletContext.getAttributeNames();
        while (e.hasMoreElements()) {
            if (e.nextElement().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Object getAttribute(String name) {
        check();
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        ScriptEnvironment env = ProviderUtil.getScriptEnvironment();
        return env.convertFromScriptObject(
                _servletContext.getAttribute(name));
    }

    public void setAttribute(String name, Object attribute) {
        check();
        if (StringUtil.isEmpty(name)) {
            return;
        }
        _servletContext.setAttribute(name, attribute);
    }

    public void removeAttribute(String name) {
        check();
        if (StringUtil.isEmpty(name)) {
            return;
        }
        _servletContext.removeAttribute(name);
    }

    // ContextAware implemetns --------------------------------------

    public void setUnderlyingContext(Object context) {
        if (context == null || context instanceof ServletContext == false) {
            throw new IllegalArgumentException();
        }
        _servletContext = (ServletContext) context;
    }

    public Object getUnderlyingContext() {
        check();
        return _servletContext;
    }

}

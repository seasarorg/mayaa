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

import javax.servlet.ServletContext;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebApplication implements Application {

    private ServletContext _servletContext;

    private void check() {
        if(_servletContext == null) {
            throw new IllegalStateException();
        }
    }

    public Object getUnderlyingObject() {
        check();
        return _servletContext;
    }
    
    public void setServletContext(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
    }
    
    public String getMimeType(String fileName) {
        check();
        if(StringUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getMimeType(fileName);
    }

    public String getRealPath(String contextRelatedPath) {
        check();
        if(StringUtil.isEmpty(contextRelatedPath)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getRealPath(contextRelatedPath);
    }

    public Scope getScope() {
        return AttributeScope.SCOPE_APPLICATION;
    }
    
    public Iterator iterateAttributeNames() {
        check();
        return EnumerationIterator.getInstance(_servletContext.getAttributeNames());
    }

    public Object getAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        return _servletContext.getAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        if(attribute != null) {
            _servletContext.setAttribute(name, attribute);
        } else {
            _servletContext.removeAttribute(name);
        }
    }
    
}

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

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletContext;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationImpl implements Application {

    private ServletContext _servletContext;
    
    public ApplicationImpl(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
    }

    public Object getUnderlyingObject() {
        return _servletContext;
    }
    
    public String getMimeType(String fileName) {
        if(StringUtil.isEmpty(fileName)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getMimeType(fileName);
    }

    public String getRealPath(String contextRelatedPath) {
        if(StringUtil.isEmpty(contextRelatedPath)) {
            throw new IllegalArgumentException();
        }
        return _servletContext.getRealPath(contextRelatedPath);
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_APPLICATION;
    }
    
    public Iterator iterateAttributeNames() {
        return EnumerationIterator.getInstance(_servletContext.getAttributeNames());
    }

    public boolean hasAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return false;
        }
        for(Enumeration e = _servletContext.getAttributeNames();
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
        return ScriptUtil.convertFromScriptObject(
                _servletContext.getAttribute(name));
    }

    public boolean isAttributeWritable() {
		return true;
	}

    public void setAttribute(String name, Object attribute) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _servletContext.setAttribute(name, attribute);
    }
    
    public void removeAttribute(String name) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _servletContext.removeAttribute(name);
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
        
}

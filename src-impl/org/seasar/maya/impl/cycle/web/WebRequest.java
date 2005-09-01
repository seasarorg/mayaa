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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.Session;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.EnumerationIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebRequest implements Request, CONST_IMPL {

    private static final long serialVersionUID = 8377365781441987529L;

    private WebSession _session;
    private HttpServletRequest _httpServletRequest;
    private String _pageName;
    private String _requestedSuffix;
    private String _extension;
    private ParamScope _param;
    private ParamValuesScope _paramValues;
    private HeaderScope _header;
    private HeaderValuesScope _headerValues;
    
    private void check() {
        if(_httpServletRequest == null) {
            throw new IllegalStateException();
        }
    }

    public String getRequestedPath() {
        String path = StringUtil.preparePath(_httpServletRequest.getServletPath()) +
            StringUtil.preparePath(_httpServletRequest.getPathInfo());
        if(StringUtil.isEmpty(path) || "/".equals(path)) {
            return SpecificationUtil.getEngineSetting(
                    WELCOME_FILE_NAME, "/index.html");
        }
        return path;
    }
    
    protected void parsePath(String path) {
        String suffixSeparator = SpecificationUtil.getEngineSetting(
                SUFFIX_SEPARATOR, "$");
        String[] parsed = StringUtil.parsePath(path, suffixSeparator);
        _pageName = parsed[0];
        _requestedSuffix = parsed[1];
        _extension = parsed[2];
    }
    
    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        if(httpServletRequest == null) {
            throw new IllegalArgumentException();
        }
        _httpServletRequest = httpServletRequest;
    }
    
    public void setForwardPath(String relativeUrlPath) {
        if(StringUtil.isEmpty(relativeUrlPath)) {
            throw new IllegalArgumentException();
        }
        parsePath(relativeUrlPath);
    }
    
    public Object getUnderlyingObject() {
        check();
        return _httpServletRequest;
    }

    public String getPageName() {
        if(_pageName == null) {
            check();
            parsePath(getRequestedPath());
        }
        return _pageName;
    }

    public String getRequestedSuffix() {
        if(_requestedSuffix == null) {
            check();
            parsePath(getRequestedPath());
        }
        return _requestedSuffix;
    }

    public String getExtension() {
        if(_extension == null) {
            check();
            parsePath(getRequestedPath());
        }
        return _extension;
    }

    public Locale[] getLocales() {
        check();
        Enumeration locales = _httpServletRequest.getLocales();
        if(locales == null) {
        	return null;
        }
        ArrayList list = new ArrayList(); 
        while(locales.hasMoreElements()) {
        	list.add(locales.nextElement());
        }
        return (Locale[])list.toArray(new Locale[list.size()]);
    }
    
    public Session getSession() {
        if(_session == null) {
            _session = new WebSession();
            _session.setHttpServletRequest(_httpServletRequest);
        }
        return _session;
    }

    public AttributeScope getParam() {
        if(_param == null) {
        	_param = new ParamScope(_httpServletRequest);
        }
        return _param;
    }

    public AttributeScope getParamValues() {
        if(_paramValues == null) {
        	_paramValues = new ParamValuesScope(_httpServletRequest);
        }
        return _paramValues;
    }

    public AttributeScope getHeader() {
        if(_header == null) {
        	_header = new HeaderScope(_httpServletRequest);
        }
        return _header;
    }

    public AttributeScope getHeaderValues() {
        if(_headerValues == null) {
        	_headerValues = new HeaderValuesScope(_httpServletRequest);
        }
        return _headerValues;
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_REQUEST;
    }
    
    public Iterator iterateAttributeNames() {
        return EnumerationIterator.getInstance(
        		_httpServletRequest.getAttributeNames());
    }

    public boolean hasAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return false;
        }
        for(Enumeration e = _httpServletRequest.getAttributeNames();
        		e.hasMoreElements(); ) {
			if(e.nextElement().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public Object getAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return null;
        }
        return _httpServletRequest.getAttribute(name);
    }

    public boolean isAttributeWritable() {
		return true;
	}

    public void setAttribute(String name, Object attribute) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletRequest.setAttribute(name, attribute);
    }
    
    public void removeAttribute(String name) {
        check();
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletRequest.removeAttribute(name);
    }
    
}

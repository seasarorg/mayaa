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
package org.seasar.maya.test.util.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.seasar.maya.impl.source.JavaSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.IteratorEnumeration;
import org.seasar.maya.impl.util.collection.NullEnumeration;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestServletContext implements ServletContext {

    private String _contextName;
    private TestCase _test;
    private Map _attributes;
    
    public TestServletContext(TestCase test, String contextName) {
        if(test == null || StringUtil.isEmpty(contextName)) {
            throw new IllegalArgumentException();
        }
        _test = test;
        _contextName = contextName;
        _attributes = new HashMap();
    }
    
    public ServletContext getContext(String contextName) {
        return new TestServletContext(_test, contextName);
    }
    
    public Object getAttribute(String name) {
        return _attributes.get(name);
    }
    
    public Enumeration getAttributeNames() {
        return new IteratorEnumeration(_attributes.keySet().iterator());
    }

    public void setAttribute(String name, Object value) {
        _attributes.put(name, value);
    }
    
    public void removeAttribute(String name) {
        _attributes.remove(name);
    }
    
    public String getInitParameter(String name) {
        return null;
    }
    
    public Enumeration getInitParameterNames() {
        return NullEnumeration.getInstance();
    }
    
    public String getMimeType(String type) {
        throw new UnsupportedOperationException();
    }
    
    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }
    
    public String getRealPath(String path) {
        throw new UnsupportedOperationException();
    }
    
    public RequestDispatcher getRequestDispatcher(String url) {
        throw new UnsupportedOperationException();
    }
    
    public URL getResource(String path) throws MalformedURLException {
        throw new UnsupportedOperationException();
    }
    
    public InputStream getResourceAsStream(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        SourceDescriptor source = new JavaSourceDescriptor(path, _test.getClass());
        if(source.exists()) {
            return source.getInputStream();
        }
        return null;
    }
    
    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException();
    }
    
    public String getServletContextName() {
        return _contextName;
    }
    
    public String getServerInfo() {
        return "Maya test context";
    }
    
    public int getMajorVersion() {
        return 2;
    }
    
    public int getMinorVersion() {
        return 3;
    }
    
    public void log(String msg, Throwable t) {
        System.out.println(msg);
        if(t != null) {
            t.printStackTrace(System.err);
        }
    }
    
    public void log(String msg) {
        System.out.println(msg);
    }

    /**
     * @deprecated
     */
    public void log(Exception e, String msg) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @deprecated
     */
    public Servlet getServlet(String arg0) throws ServletException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @deprecated
     */
    public Enumeration getServlets() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @deprecated
     */
    public Enumeration getServletNames() {
        throw new UnsupportedOperationException();
    }

}

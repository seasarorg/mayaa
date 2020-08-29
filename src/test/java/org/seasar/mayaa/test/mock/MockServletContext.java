/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.test.mock;

import java.io.File;
import java.io.IOException;
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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.IteratorEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MockServletContext implements ServletContext {

    private static final Log LOG =
        LogFactory.getLog(MockServletContext.class);

    private static Map _mimeTypes;
    static {
        _mimeTypes = new HashMap();
        _mimeTypes.put("htm", "text/html");
        _mimeTypes.put("html", "text/html");
        _mimeTypes.put("xhtml", "text/xhtml");
        _mimeTypes.put("xml", "text/xml");
        _mimeTypes.put("txt", "text/plain");
        _mimeTypes.put("jsp", "text/jsp");
        _mimeTypes.put("css", "text/css");
        _mimeTypes.put("gif", "image/gif");
        _mimeTypes.put("jpg", "image/jpg");
        _mimeTypes.put("png", "text/png");
    }

    private String _contextName;

    private Class<?> _test;

    private Map _attributes;

    private Map _initParams;

    private File _baseDir;

    public MockServletContext(Class<?> test) {
        this(test, null);
    }

    private MockServletContext(Class<?> test, String contextName) {
        if (test == null) {
            throw new IllegalArgumentException();
        }
        _test = test;
        if (StringUtil.isEmpty(contextName)) {
            Package pack = test.getClass().getPackage();
            String packName = pack.getName();
            int pos = packName.lastIndexOf('.');
            if (pos != -1) {
                packName = packName.substring(pos + 1);
            }
            contextName = packName;
        }
        _contextName = contextName;
        _attributes = new HashMap();
    }

    private File getBaseDir() {
        if (_baseDir == null) {
            Class<?> testClass = _test;
            String resourceName = testClass.getName().replace('.', '/') + ".class";
            ClassLoader loader = testClass.getClassLoader();
            URL url = loader.getResource(resourceName);
            File file = new File(url.getPath().toString());
            return file.getParentFile();
        }
        return _baseDir;
    }

    public ServletContext getContext(String contextName) {
        return new MockServletContext(_test, contextName);
    }

    public Object getAttribute(String name) {
        return _attributes.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        return IteratorEnumeration.getInstance(_attributes.keySet().iterator());
    }

    public void setAttribute(String name, Object value) {
        _attributes.put(name, value);
    }

    public void removeAttribute(String name) {
        _attributes.remove(name);
    }

    public void addInitParameter(String name, String value) {
        if (StringUtil.isEmpty(name) || value == null) {
            throw new IllegalArgumentException();
        }
        if (_initParams == null) {
            _initParams = new HashMap();
        }
        _initParams.put(name, value);
    }

    public String getInitParameter(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if (_initParams == null) {
            return null;
        }
        return (String) _initParams.get(name);
    }

    public Enumeration getInitParameterNames() {
        return IteratorEnumeration.getInstance(_initParams.keySet().iterator());
    }

    public String getMimeType(String path) {
        if (path == null) {
            throw new IllegalArgumentException();
        }
        int pos = path.lastIndexOf('.');
        if (pos != -1) {
            path = path.substring(pos + 1);
        }
        path = path.toLowerCase();
        String type = (String) _mimeTypes.get(path);
        if (type == null) {
            type = "text/plain";
        }
        return type;
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    public String getRealPath(String path) {
        path = StringUtil.preparePath(path);
        path = path.replace('/', File.separatorChar);
        return getBaseDir() + path;
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        throw new UnsupportedOperationException();
    }

    public URL getResource(String path) throws MalformedURLException {
        return new URL(getRealPath(path));
    }

    public InputStream getResourceAsStream(String path) {
        if (StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        try {
            URL url = getResource(path);
            return url.openStream();
        } catch (IOException e) {
            // do nothing.
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
        return "MockServletContext";
    }

    public int getMajorVersion() {
        return 2;
    }

    public int getMinorVersion() {
        return 3;
    }

    public void log(String msg, Throwable t) {
        if (LOG.isInfoEnabled()) {
            LOG.info(msg, t);
        }
    }

    public void log(String msg) {
        if (LOG.isInfoEnabled()) {
            LOG.info(msg);
        }
    }

    public void log(Exception e, String msg) {
        throw new UnsupportedOperationException();
    }

    public Servlet getServlet(String arg0) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getServlets() {
        throw new UnsupportedOperationException();
    }

    public Enumeration getServletNames() {
        throw new UnsupportedOperationException();
    }

}

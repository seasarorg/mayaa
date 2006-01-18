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
package org.seasar.mayaa.impl.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.DelaySourceDescriptor;
import org.seasar.mayaa.impl.source.PageSourceDescriptor;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl
        implements Engine, CONST_IMPL {

    private static final long serialVersionUID = 1428444571422324206L;
    private static final Log LOG = LogFactory.getLog(EngineImpl.class);
    private static final String DEFAULT_SPECIFICATION =
        "defaultSpecification";

    private ErrorHandler _errorHandler;
    private List _pages;
    private String _defaultSpecification = "";
    private List _templatePathPatterns;

    public void setErrorHandler(ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        if (_errorHandler == null) {
            throw new IllegalStateException();
        }
        return _errorHandler;
    }

    protected Page findPageFromCache(String pageName) {
        if (_pages != null) {
            synchronized (_pages) {
                for (Iterator it = new ChildSpecificationsIterator(_pages);
                        it.hasNext();) {
                    Object child = it.next();
                    if (child instanceof Page) {
                        Page page = (Page) child;
                        synchronized (page) {
                            String name = page.getPageName();
                            if (pageName.equals(name)) {
                                page.checkTimestamp();
                                return page;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Page createNewPage(String pageName) {
        Page page = new PageImpl(pageName);
        String path = pageName + ".mayaa";
        SourceDescriptor source = SourceUtil.getSourceDescriptor(path);
        page.setSource(source);
        return page;
    }

    public Page getPage(String pageName) {
        synchronized (this) {
            Page page = findPageFromCache(pageName);
            if (page == null) {
                page = createNewPage(pageName);
                if (_pages == null) {
                    _pages = new ArrayList();
                }
                page.checkTimestamp();
                _pages.add(new SoftReference(page));
            }
            return page;
        }
    }

    protected boolean isPageRequested() {
        RequestScope request = CycleUtil.getRequestScope();
        if ("mayaa".equals(request.getExtension())) {
            return true;
        }

        return validPath(request.getRequestedPath());
    }

    private boolean validPath(String path) {
        if (_templatePathPatterns != null) {
            for (Iterator it = _templatePathPatterns.iterator();
                    it.hasNext();) {
                PathPattern pattern = (PathPattern) it.next();
                if (pattern.matches(path)) {
                    return pattern.isTemplate();
                }
            }
        }
        return true;
    }

    protected Throwable removeWrapperRuntimeException(Throwable t) {
        Throwable throwable = t;
        while (throwable.getClass().equals(RuntimeException.class)) {
            if (throwable.getCause() == null) {
                break;
            }
            throwable = throwable.getCause();
        }
        return throwable;
    }

    public void handleError(Throwable t, boolean pageFlush) {
        t = removeWrapperRuntimeException(t);
        try {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.setHandledError(t);
            getErrorHandler().doErrorHandle(t, pageFlush);
            cycle.setHandledError(null);
        } catch (Throwable internal) {
            if (LOG.isFatalEnabled()) {
                String fatalMsg = StringUtil.getMessage(
                        EngineImpl.class, 0, internal.getMessage());
                LOG.fatal(fatalMsg, internal);
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t);
        }
    }

    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }

    protected void doPageService(ServiceCycle cycle, boolean pageFlush) {
        checkTimestamp();
        try {
            boolean service = true;
            while (service) {
                try {
                    saveToCycle();
                    SpecificationUtil.initScope();
                    SpecificationUtil.startScope(null);
                    SpecificationUtil.execEvent(this, QM_BEFORE_RENDER);
                    RequestScope request = cycle.getRequestScope();
                    String pageName = request.getPageName();
                    String requestedSuffix = request.getRequestedSuffix();
                    String extension = request.getExtension();
                    Page page = getPage(pageName);
                    ProcessStatus ret =
                            page.doPageRender(requestedSuffix, extension);
                    saveToCycle();
                    SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
                    SpecificationUtil.endScope();
                    Response response = CycleUtil.getResponse();
                    if (ret == null) {
                        if (response.getWriter().isDirty() == false) {
                            throw new RenderNotCompletedException(
                                    pageName, extension);
                        }
                    }
                    if (pageFlush) {
                        response.flush();
                    }
                    service = false;
                } catch (PageForwarded f) {
                    // do nothing.
                }
            }
        } catch (Throwable t) {
            cycle.getResponse().clearBuffer();
            SpecificationUtil.initScope();
            handleError(t, pageFlush);
        }
    }

    protected void doResourceService(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        String path = cycle.getRequestScope().getRequestedPath();
        PageSourceDescriptor source = new PageSourceDescriptor();
        source.setSystemID(path);
        InputStream stream = source.getInputStream();
        if (stream != null) {
            OutputStream out = cycle.getResponse().getOutputStream();
            try {
                for (int i = stream.read(); i != -1; i = stream.read()) {
                    out.write(i);
                }
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtil.close(stream);
            }
        } else {
            cycle.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void doService(boolean pageFlush) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (isPageRequested()) {
            doPageService(cycle, pageFlush);
        } else {
            doResourceService(cycle);
        }
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if (DEFAULT_SPECIFICATION.equals(name)) {
            SourceDescriptor source = new DelaySourceDescriptor();
            source.setSystemID(value);
            setSource(source);
            _defaultSpecification = value;
        } else if (TEMPLATE_PATH_PATTERN.equals(name)) {
            if (StringUtil.hasValue(value)) {
                if (_templatePathPatterns == null) {
                    _templatePathPatterns = new LinkedList();
                }
                PathPattern pathPattern =
                    new PathPattern(Pattern.compile(value), true);
                _templatePathPatterns.add(0, pathPattern);
            }
        } else if (NOT_TEMPLATE_PATH_PATTERN.equals(name)) {
            if (StringUtil.hasValue(value)) {
                if (_templatePathPatterns == null) {
                    _templatePathPatterns = new LinkedList();
                }
                PathPattern pathPattern =
                    new PathPattern(Pattern.compile(value), false);
                _templatePathPatterns.add(0, pathPattern);
            }
        }
        super.setParameter(name, value);
    }

    public String getParameter(String name) {
        if (DEFAULT_SPECIFICATION.equals(name)) {
            return _defaultSpecification;
        } else if (TEMPLATE_PATH_PATTERN.equals(name)) {
            if (_templatePathPatterns == null) {
                return null;
            }
            return patternToString(_templatePathPatterns, true);
        } else if (NOT_TEMPLATE_PATH_PATTERN.equals(name)) {
            if (_templatePathPatterns == null) {
                return null;
            }
            return patternToString(_templatePathPatterns, false);
        }
        return super.getParameter(name);
    }

    private String patternToString(List patterns, boolean result) {
        StringBuffer sb = new StringBuffer();
        for (Iterator it = patterns.iterator(); it.hasNext();) {
            PathPattern pathPattern = (PathPattern) it.next();
            if (pathPattern.isTemplate() == result) {
                sb.append(pathPattern.getPattern());
                sb.append("|");
            }
        }
        return sb.toString();
    }

    //---- support class

    private class PathPattern {
        private Pattern _pattern;
        private boolean _result;

        public PathPattern(Pattern pattern, boolean result) {
            _pattern = pattern;
            _result = result;
        }

        public boolean matches(String path) {
            return _pattern.matcher(path).matches();
        }

        public boolean isTemplate() {
            return _result;
        }

        public String getPattern() {
            return _pattern.pattern();
        }
    }

}

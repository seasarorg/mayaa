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
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.cycle.Response;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.RequestScope;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.error.ErrorHandler;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.DelaySourceDescriptor;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl
        implements Engine, CONST_IMPL {

    private static final long serialVersionUID = 1428444571422324206L;
    private static final Log LOG = LogFactory.getLog(EngineImpl.class);
    private static final String DEFAULT_SPECIFICATION = "defaultSpecification";
    private static final String PAGE_CLASS = "pageClass";
    private static final String TEMPLATE_CLASS = "templateClass";

    private ErrorHandler _errorHandler;
    private List _pages;
    private String _defaultSpecification = "";
    private List _templatePathPatterns;
    private Class _pageClass = PageImpl.class;
    private Class _templateClass = TemplateImpl.class;

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
        Page page = createPageInstance(pageName);
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

    public boolean isPageRequested() {
        RequestScope request = CycleUtil.getRequestScope();
        if ("mayaa".equals(request.getExtension())) {
            return true;
        }

        return validPath(request.getRequestedPath(), request.getMimeType());
    }

    private boolean validPath(String path, String mimeType) {
        if (_templatePathPatterns != null) {
            for (Iterator it = _templatePathPatterns.iterator();
                    it.hasNext();) {
                PathPattern pattern = (PathPattern) it.next();
                if (pattern.matches(path)) {
                    return pattern.isTemplate();
                }
            }
        }
        return mimeType != null
            && (mimeType.indexOf("html") != -1 || mimeType.indexOf("xml") != -1);
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
        Throwable error = removeWrapperRuntimeException(t);
        try {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.setHandledError(error);
            getErrorHandler().doErrorHandle(error, pageFlush);
            cycle.setHandledError(null);
        } catch (RenderingTerminated ignore) {
            // do nothing.
        } catch (PageForwarded pf) {
            // return page service
            doPageService(CycleUtil.getServiceCycle(), null, pageFlush);
        } catch (Throwable internal) {
            if (internal instanceof PageNotFoundException) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(error.getMessage());
                }
            } else if (LOG.isFatalEnabled()) {
                String fatalMsg = StringUtil.getMessage(
                        EngineImpl.class, 0, internal.getMessage());
                LOG.fatal(fatalMsg, internal);
            }
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            }
            throw new RuntimeException(error);
        }
    }

    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }

    protected void doPageService(
            ServiceCycle cycle, Map pageScopeValues, boolean pageFlush) {
        checkTimestamp();
        try {
            boolean service = true;
            while (service) {
                try {
                    String pageName = null;
                    String extension = null;
                    ProcessStatus ret = null;

                    saveToCycle();
                    SpecificationUtil.initScope();
                    SpecificationUtil.startScope(pageScopeValues);
                    try {
                        SpecificationUtil.execEvent(this, QM_BEFORE_RENDER);
                        RequestScope request = cycle.getRequestScope();
                        pageName = request.getPageName();
                        String requestedSuffix = request.getRequestedSuffix();
                        extension = request.getExtension();
                        Page page = getPage(pageName);
                        ret = page.doPageRender(requestedSuffix, extension);
                        saveToCycle();
                        SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
                    } finally {
                        SpecificationUtil.endScope();
                    }
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
                } catch (RenderingTerminated rt) {
                    service = false;
                } catch (PageForwarded ignore) {
                    // do nothing.
                }
            }
        } catch (Throwable t) {
            cycle.getResponse().clearBuffer();
            SpecificationUtil.initScope();
            handleError(t, pageFlush);
            saveToCycle();
        }
    }

    protected void doResourceService(ServiceCycle cycle) {
        if (cycle == null) {
            throw new IllegalArgumentException();
        }
        String path = cycle.getRequestScope().getRequestedPath();
        SourceDescriptor source = SourceUtil.getSourceDescriptor(path);

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
        doService(null, pageFlush);
    }

    public void doService(Map pageScopeValues, boolean pageFlush) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (isPageRequested()) {
            doPageService(cycle, pageScopeValues, pageFlush);
        } else {
            doResourceService(cycle);
        }
    }

    public Page createPageInstance(String pageName) {
        Page page = (Page) ObjectUtil.newInstance(getPageClass());
        page.initialize(pageName);
        return page;
    }

    public Template createTemplateInstance(
            Page page, String suffix, String extension) {
        Template template = (Template) ObjectUtil.newInstance(getTemplateClass());
        template.initialize(page, suffix, extension);
        return template;
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
        } else if (PAGE_CLASS.equals(name)) {
            if (StringUtil.hasValue(value)) {
                Class pageClass = ObjectUtil.loadClass(value);
                if (Page.class.isAssignableFrom(pageClass)) {
                    _pageClass = pageClass;
                }
            }
        } else if (TEMPLATE_CLASS.equals(name)) {
            if (StringUtil.hasValue(value)) {
                Class templateClass = ObjectUtil.loadClass(value);
                if (Template.class.isAssignableFrom(templateClass)) {
                    _templateClass = templateClass;
                }
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
        } else if (PAGE_CLASS.equals(name)) {
            return _pageClass.getName();
        } else if (TEMPLATE_CLASS.equals(name)) {
            return _templateClass.getName();
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

    private Class getPageClass() {
        return _pageClass;
    }

    private Class getTemplateClass() {
        return _templateClass;
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

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
package org.seasar.mayaa.impl.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
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
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.DelaySourceDescriptor;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.impl.util.DateFormatPool;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl
        implements Engine {

    private static final long serialVersionUID = 643873507172011552L;

    static final Log LOG = LogFactory.getLog(EngineImpl.class);

    public static final String PAGE_CLASS = "pageClass";
    public static final String TEMPLATE_CLASS = "templateClass";
    public static final String PAGE_SERIALIZE = "pageSerialize";
    public static final String SURVIVE_LIMIT = "surviveLimit";
    public static final String FORWARD_LIMIT = "forwardLimit";
    public static final String REQUESTED_SUFFIX_ENABLED = "requestedSuffixEnabled";
    public static final String DUMP_ENABLED = "dumpEnabled";
    public static final String CONVERT_CHARSET = "convertCharset";
    public static final String NO_CACHE_VALUE = "noCacheValue";
    private static final boolean DEFAULT_PAGE_SERIALIZE = true;

    private transient Specification _defaultSpecification;
    private transient ErrorHandler _errorHandler;
    private transient SpecificationCache _specCache;
    /** Engineが破棄されていればtrue。破棄後はサービスを保証しない。 */
    private volatile boolean _destroyed = false;

    // parameters
    private String _defaultSpecificationID = "/default.mayaa";
    private List _templatePathPatterns;
    private Class _pageClass = PageImpl.class;
    private Class _templateClass = TemplateImpl.class;
    private int _surviveLimit = 5;
    private boolean _requestedSuffixEnabled = false;
    private boolean _dumpEnabled = false;
    private int _forwardLimit = 10;
    private String _mayaaExtension = ".mayaa";
    private String _noCacheValue = "no-cache";

    // change on setParameter
    private String _mayaaExtensionName = "mayaa";
    private String _defaultPageName = "/default";
    private boolean _defaultIsMayaa = false;

    public EngineImpl() {
        setSpecificationSerialize(DEFAULT_PAGE_SERIALIZE);
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
        if (_errorHandler == null) {
            throw new IllegalStateException();
        }
        return _errorHandler;
    }

    public Specification findSpecificationFromCache(String systemID) {
        return getCache().get(systemID);
    }

    protected Page findPageFromCache(String pageName) {
        return (Page) findSpecificationFromCache(pageName + _mayaaExtension);
    }

    public Page getPage(String pageName) {
        if (_defaultIsMayaa && _defaultPageName.equals(pageName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(StringUtil.getMessage(EngineImpl.class, 2));
            }
            RequestScope request = CycleUtil.getRequestScope();
            throw new PageNotFoundException(pageName, request.getExtension());
        }

        Page page = findPageFromCache(pageName);
        if (page == null) {
            page = createPageInstance(pageName);
        }
        return page;
    }

    public boolean isPageRequested() {
        RequestScope request = CycleUtil.getRequestScope();
        if (_mayaaExtensionName.equals(request.getExtension())) {
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

    public void handleError(Throwable thrown, boolean pageFlush) {
        Throwable throwable = removeWrapperRuntimeException(thrown);
        if (EngineUtil.isClientAbortException(throwable)) {
            // client abort は出力しようがないので無視
            return;
        }
        if (throwable instanceof CyclicForwardException) {
            // 循環forwardはそのまま投げる
            throw (CyclicForwardException) throwable;
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        try {
            cycle.setHandledError(throwable);
            getErrorHandler().doErrorHandle(throwable, pageFlush);
        } catch (RenderingTerminated ignore) {
            // do nothing.
        } catch (PageForwarded pf) {
            // return page service
            checkCyclicForward(cycle.getRequestScope());
            doPageService(cycle, null, pageFlush);
        } catch (Throwable internalThrown) {
            Throwable internal = removeWrapperRuntimeException(internalThrown);
            if (internal instanceof PageNotFoundException) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(throwable.getMessage());
                }
            } else {
                if (EngineUtil.isClientAbortException(internal)) {
                    // client abort は出力しようがないので無視
                    return;
                }
                if (LOG.isFatalEnabled()) {
                    String fatalMsg = StringUtil.getMessage(
                            EngineImpl.class, 0, internal.getMessage());
                    LOG.fatal(fatalMsg, internal);
                }
            }

            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new RuntimeException(throwable);
        } finally {
            cycle.setHandledError(null);
        }
    }

    protected void checkCyclicForward(RequestScope request) {
        if (_forwardLimit <= 0) {
            return;
        }

        String key = CyclicForwardException.class.getName();
        Map pathMap = (Map) request.getAttribute(key);
        if (pathMap == null) {
            pathMap = new HashMap();
            request.setAttribute(key, pathMap);
        }

        String pageName = getRequestedPageName(request);
        Integer count = (Integer) pathMap.get(pageName);
        int countInt = (count != null) ? count.intValue() + 1 : 1;
        if (countInt > _forwardLimit) {
            throw new CyclicForwardException(pageName);
        }
        pathMap.put(pageName, new Integer(countInt));
    }

    protected String getRequestedPageName(RequestScope request) {
        StringBuffer sb = new StringBuffer();
        sb.append(request.getPageName());
        if (StringUtil.hasValue(request.getRequestedSuffix())) {
            sb.append(getSuffixSeparator());
            sb.append(request.getRequestedSuffix());
        }
        sb.append('.');
        sb.append(request.getExtension());
        return sb.toString();
    }

    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }

    public void build() {
        build(true);
    }

    public void build(boolean rebuild) {
        synchronized(this) {
            if (_defaultSpecification != null) {
                if (_defaultSpecification.isDeprecated() == false) {
                    return;
                }
                releaseDefaultSpecification();
            }
            if (StringUtil.hasValue(_defaultSpecificationID)) {
                _defaultSpecification =
                    createDefaultSpecification(_defaultSpecificationID);
            }
        }
    }

    // NodeTreeWalker override

    public void addChildNode(NodeTreeWalker childNode) {
        if (_defaultSpecification != null) {
            _defaultSpecification.addChildNode(childNode);
            return;
        }
        throw new UnsupportedOperationException();
    }

    public NodeTreeWalker getChildNode(int index) {
        if (_defaultSpecification != null) {
            return _defaultSpecification.getChildNode(index);
        }
        throw new UnsupportedOperationException();
    }

    public int getChildNodeSize() {
        if (_defaultSpecification != null) {
            return _defaultSpecification.getChildNodeSize();
        }
        throw new UnsupportedOperationException();
    }

    public void clearChildNodes() {
        if (_defaultSpecification != null) {
            _defaultSpecification.clearChildNodes();
        }
        throw new UnsupportedOperationException();
    }

    protected void releaseDefaultSpecification() {
        _defaultSpecification = null;
    }

    public void insertChildNode(int index, NodeTreeWalker childNode) {
        if (_defaultSpecification != null) {
            _defaultSpecification.insertChildNode(index, childNode);
        }
        throw new UnsupportedOperationException();
    }

    public boolean removeChildNode(NodeTreeWalker node) {
        if (_defaultSpecification != null) {
            _defaultSpecification.removeChildNode(node);
        }
        throw new UnsupportedOperationException();
    }

    public Iterator iterateChildNode() {
        if (_defaultSpecification != null) {
            return _defaultSpecification.iterateChildNode();
        }
        throw new UnsupportedOperationException();
    }

    public Date getTimestamp() {
        if (_defaultSpecification != null) {
            return _defaultSpecification.getTimestamp();
        }
        return null;
    }

    public boolean isDeprecated() {
        if (_defaultSpecification != null) {
            return _defaultSpecification.isDeprecated();
        }
        return false;
    }

    protected void doPageService(
            ServiceCycle cycle, Map pageScopeValues, boolean pageFlush) {
        build();
        try {
            boolean service = true;
            while (service) {
                try {
                    String pageName = null;
                    String extension = null;
                    String requestedSuffix = null;
                    ProcessStatus ret = null;

                    saveToCycle();
                    SpecificationUtil.initScope();
                    SpecificationUtil.startScope(pageScopeValues);
                    try {
                        SpecificationUtil.execEvent(_defaultSpecification, QM_BEFORE_RENDER);
                        RequestScope request = cycle.getRequestScope();
                        pageName = request.getPageName();
                        extension = request.getExtension();
                        if (_requestedSuffixEnabled) {
                            requestedSuffix = request.getRequestedSuffix();
                        }

                        Page page = getPage(pageName);
                        ret = page.doPageRender(requestedSuffix, extension);

                        saveToCycle();
                        SpecificationUtil.execEvent(_defaultSpecification, QM_AFTER_RENDER);
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
                    checkCyclicForward(cycle.getRequestScope());
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
        String[] modifiedSinces = (String[])cycle.getRequestScope()
            .getHeaderValues().getAttribute("If-Modified-Since");
        if (modifiedSinces != null && modifiedSinces.length > 0) {
            String modifiedSince = modifiedSinces[0];
            Date sinceDate = null;
            DateFormat format = DateFormatPool.borrowRFC1123Format();
            try {
                sinceDate = format.parse(modifiedSince);
            } catch(ParseException e) {
                DateFormat format2 = DateFormatPool.borrowRFC2822Format();
                try {
                    sinceDate = format2.parse(modifiedSince);
                } catch(ParseException e2) {
                    // give up
                } finally {
                    DateFormatPool.returnFormat(format2);
                }
            } finally {
                DateFormatPool.returnFormat(format);
            }
            Date sourceTimestamp = new Date(
                    source.getTimestamp().getTime() / 1000 * 1000);
            if (sinceDate != null
                    && sourceTimestamp.after(sinceDate) == false) {
                cycle.getResponse().setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        InputStream stream = source.getInputStream();
        if (stream != null) {
            DateFormat format = DateFormatPool.borrowRFC1123Format();
            try {
                cycle.getResponse().setHeader("Last-Modified",
                        format.format(source.getTimestamp()));
            } finally {
                DateFormatPool.returnFormat(format);
            }
            OutputStream out = cycle.getResponse().getOutputStream();
            try {
                byte[] buffer = new byte[512];
                int readln;
                while ((readln = stream.read(buffer)) != -1) {
                    out.write(buffer, 0, readln);
                }
                out.flush();
            } catch (IOException e) {
                if (EngineUtil.isClientAbortException(e)) {
                    stream = null;
                } else {
                    throw new RuntimeException(e);
                }
            } finally {
                if (stream != null) {
                    IOUtil.close(stream);
                }
            }
        } else {
            cycle.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public void doService(Map pageScopeValues, boolean pageFlush) {
        if (_destroyed) {
            throw new IllegalStateException(StringUtil.getMessage(EngineImpl.class, 1));
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (isPageRequested()) {
            doPageService(cycle, pageScopeValues, pageFlush);

            if (_dumpEnabled) {
                dump(cycle);
            }
        } else {
            doResourceService(cycle);
        }
    }

    protected void dump(ServiceCycle cycle) {
        ProcessorDump dump = new ProcessorDump();
        dump.setPrintContents(true);
        RequestScope request = cycle.getRequestScope();
        String pageName = request.getPageName();
        Page page = getPage(pageName);
        dump.printSource(page);
    }

    protected SpecificationCache getCache() {
        if (_specCache == null) {
            _specCache = new SpecificationCache(_surviveLimit);
        }
        return _specCache;
    }

    private static interface SpecificationGenerator {
        Class getInstantiator(SourceDescriptor source);
        void initialize(Specification instance);
    }

    protected Specification createSpecificationInstance(String systemID,
            boolean registerCache,
            SpecificationGenerator generator) {
        boolean rebuild = false;
        Specification spec;
        if (isSpecificationSerialize()) {
            spec = deserialize(systemID);
            if (spec != null) {
                if (spec.isDeprecated() == false) {
                    if (registerCache) {
                        getCache().add(spec);
                    }
                    return spec;
                }
                rebuild = true;
            }
        }
        SourceDescriptor source =
            SourceUtil.getSourceDescriptor(systemID);
        Class specClass = generator.getInstantiator(source);
        if (specClass == null) {
            return null;
        }
        spec = (Specification) ObjectUtil.newInstance(specClass);
        generator.initialize(spec);
        if (spec instanceof SpecificationImpl) {
            ((SpecificationImpl)spec).setSpecificationSerialize(
                    isSpecificationSerialize());
        }
        spec.setSource(source);
        spec.setSystemID(systemID);
        try {
            SpecificationUtil.startScope(null);
            try {
                spec.build(rebuild);
            } finally {
                SpecificationUtil.endScope();
            }
        } catch(RuntimeException e) {
            throw e;
        }
        if (registerCache) {
            getCache().add(spec);
        }
        return spec;
    }

    public Specification createDefaultSpecification(final String systemID) {
        return createSpecificationInstance(systemID, false,
                new SpecificationGenerator() {
                    public Class getInstantiator(SourceDescriptor source) {
                        return SpecificationImpl.class;
                    }
                    public void initialize(Specification instance) {
                        // no-op
                    }
                });
    }

    public Page createPageInstance(final String pageName) {
        return (Page) createSpecificationInstance(
                pageName + _mayaaExtension, true,
                new SpecificationGenerator() {
            public Class getInstantiator(SourceDescriptor source) {
                return getPageClass();
            }
            public void initialize(Specification instance) {
                ((Page) instance).initialize(pageName);
            }
        });
    }

    public Template createTemplateInstance(
            final Page page, final String suffix, final String extension) {
        return (Template) createSpecificationInstance(
                getTemplateID(page, suffix, extension), true,
                new SpecificationGenerator() {
            public Class getInstantiator(SourceDescriptor source) {
                if (source.exists() == false) {
                    return null;
                }
                return getTemplateClass();
            }
            public void initialize(Specification instance) {
                ((Template) instance).initialize(page, suffix, extension);
            }
        });
    }

    public String getTemplateID(Page page, String suffix, String extension) {
        StringBuffer name = new StringBuffer(page.getPageName());
        if (StringUtil.hasValue(suffix)) {
            String separator = getSuffixSeparator();
            name.append(separator).append(suffix);
        }
        if (StringUtil.hasValue(extension)) {
            name.append(".").append(extension);
        }
        return name.toString();
    }

    protected String getSuffixSeparator() {
        return EngineUtil.getEngineSetting(SUFFIX_SEPARATOR, "$");
    }

    public synchronized void destroy() {
        _destroyed = true;
        releaseDefaultSpecification();
        if (_specCache != null) {
            _specCache.release();
        }
    }

    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if (DEFAULT_SPECIFICATION.equals(name)) {
            if (StringUtil.hasValue(value)) {
                String systemID;
                if (value.charAt(0) != '/') {
                    systemID = "/" + value;
                } else {
                    systemID = value;
                }
                SourceDescriptor source = new DelaySourceDescriptor();
                source.setSystemID(systemID);
                setSource(source);
                _defaultSpecificationID = systemID;

                setupDefaultIsMayaa();
            }
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
        } else if (MAYAA_EXTENSION.equals(name)) {
            if (StringUtil.hasValue(value)) {
                if (value.startsWith(".")) {
                    _mayaaExtension = value;
                    _mayaaExtensionName = value.substring(1);
                } else {
                    _mayaaExtension = "." + value;
                    _mayaaExtensionName = value;
                }

                setupDefaultIsMayaa();
            }
        } else if (TEMPLATE_CLASS.equals(name)) {
            if (StringUtil.hasValue(value)) {
                Class templateClass = ObjectUtil.loadClass(value);
                if (Template.class.isAssignableFrom(templateClass)) {
                    _templateClass = templateClass;
                }
            }
        } else if (NO_CACHE_VALUE.equals(name)) {
            _noCacheValue = value;
        } else if (PAGE_SERIALIZE.equals(name)) {
            setSpecificationSerialize(
                    Boolean.valueOf(value).booleanValue());
        } else if (SURVIVE_LIMIT.equals(name)) {
            _surviveLimit = Integer.parseInt(value);
        } else if (FORWARD_LIMIT.equals(name)) {
            _forwardLimit = Integer.parseInt(value);
        } else if (REQUESTED_SUFFIX_ENABLED.equals(name)) {
            _requestedSuffixEnabled = Boolean.valueOf(value).booleanValue();
        } else if (DUMP_ENABLED.equals(name)) {
            _dumpEnabled = Boolean.valueOf(value).booleanValue();
        } else if (CONVERT_CHARSET.equals(name)) {
            CharsetConverter.setEnabled(Boolean.valueOf(value).booleanValue());
        }
        super.setParameter(name, value);
    }

    private void setupDefaultIsMayaa() {
        _defaultIsMayaa = _defaultSpecificationID.endsWith(_mayaaExtension);
        if (_defaultIsMayaa) {
            _defaultPageName = _defaultSpecificationID.substring(
                    0, _defaultSpecificationID.lastIndexOf('.'));
        }
    }

    public String getParameter(String name) {
        if (DEFAULT_SPECIFICATION.equals(name)) {
            return _defaultSpecificationID;
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
        } else if (MAYAA_EXTENSION.equals(name)) {
            return _mayaaExtension;
        } else if (PAGE_CLASS.equals(name)) {
            return _pageClass.getName();
        } else if (TEMPLATE_CLASS.equals(name)) {
            return _templateClass.getName();
        } else if (NO_CACHE_VALUE.equals(name)) {
            return _noCacheValue;
        } else if (PAGE_SERIALIZE.equals(name)) {
            return String.valueOf(isSpecificationSerialize());
        } else if (SURVIVE_LIMIT.equals(name)) {
            return String.valueOf(_surviveLimit);
        } else if (FORWARD_LIMIT.equals(name)) {
            return String.valueOf(_forwardLimit);
        } else if (REQUESTED_SUFFIX_ENABLED.equals(name)) {
            return String.valueOf(_requestedSuffixEnabled);
        } else if (DUMP_ENABLED.equals(name)) {
            return String.valueOf(_dumpEnabled);
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

    protected Class getPageClass() {
        return _pageClass;
    }

    protected Class getTemplateClass() {
        return _templateClass;
    }

    //---- support class

    private static class PathPattern {
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

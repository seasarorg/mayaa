/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.RequestScope;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.source.DelaySourceDescriptor;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl
        implements Engine, CONST_IMPL {
    
	private static final long serialVersionUID = 1428444571422324206L;
    private static final Log LOG = LogFactory.getLog(EngineImpl.class);
    private static Set _paramNames;
    static {
        _paramNames = new HashSet();
        _paramNames.add(CHECK_TIMESTAMP);
        _paramNames.add(SUFFIX_SEPARATOR);
        _paramNames.add(WELCOME_FILE_NAME);
        _paramNames.add(DECODE_PROCESSOR_TREE);
    }
    public static final String THROWABLE = "THROWABLE";

    private Map _parameters;
    private ErrorHandler _errorHandler;
    private List _pages;
    
    public void setParameter(String name, String value) {
        if("defaultSpecification".equals(name)) {
            SourceDescriptor source = new DelaySourceDescriptor();
            source.setSystemID(value);
            setSource(source);
        } else if(_paramNames.contains(name)) {
            if(_parameters == null) {
                _parameters = new HashMap();
            }
            if(StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _parameters.put(name, value);
        } else {
            throw new UnsupportedParameterException(getClass(), name);
        }
    }

    public String getParameter(String name) {
        if(_paramNames.contains(name)) {
            if(_parameters == null) {
                return null;
            }
            return (String)_parameters.get(name);
        }
        throw new UnsupportedParameterException(getClass(), name);
    }
    
    public void setErrorHandler(ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
	    if(_errorHandler == null) {
            throw new IllegalStateException();
	    }
        return _errorHandler;
    }
    
    protected Page findPageFromCache(String pageName) {
        if(_pages != null) {
            for(Iterator it = new ChildSpecificationsIterator(_pages);
                    it.hasNext(); ) {
                Object child = it.next();
                if(child instanceof Page) {
                    Page page = (Page)child;
                    String name = page.getPageName();
                    if(pageName.equals(name)) {
                        return page;
                    }
                }
            }
        }
        return null;
    }
    
    protected Page createNewPage(String pageName) {
    	Page page = new PageImpl(pageName);
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        String path = pageName + ".maya";
        SourceDescriptor source = provider.getPageSourceDescriptor(path);
        page.setSource(source);
        return page;
    }
    
    public Page getPage(String pageName) {
        synchronized(this) {
        	Page page = findPageFromCache(pageName);
        	if(page == null) {
	        	page = createNewPage(pageName);
	            if(_pages == null) {
	                _pages = new ArrayList();
	            }
	            _pages.add(new SoftReference(page));
        	}
            return page;
        }
    }
   
	public void doService() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(isPageRequested()) {
            doPageService(cycle);
        } else {
            doResourceService(cycle);
        }
	}

    protected boolean isPageRequested() {
        RequestScope request = CycleUtil.getServiceCycle().getRequestScope();
        if ("maya".equals(request.getExtension())) {
            return true;
        }

        String mimeType = request.getMimeType();
        return mimeType != null
                && (mimeType.indexOf("html") != -1
                        || mimeType.indexOf("xml") != -1);
    }

    protected Throwable removeWrapperRuntimeException(Throwable t) {
        Throwable throwable = t ;
        while(throwable.getClass().equals(RuntimeException.class)) {
            if( throwable.getCause() == null ) {
                break;
            }
            throwable = throwable.getCause();
        }
        return throwable ;
    }
    
    protected void handleError(Throwable t) {
        t = removeWrapperRuntimeException(t);
        try {
            RequestScope request = CycleUtil.getRequestScope();
            request.setAttribute(THROWABLE, t);
            getErrorHandler().doErrorHandle(t);
            request.removeAttribute(THROWABLE);
            CycleUtil.getResponse().flushAll();
        } catch(Throwable internal) {
            if(LOG.isFatalEnabled()) {
                String fatalMsg = StringUtil.getMessage(
                        EngineImpl.class, 0, new String[] { internal.getMessage() });
                LOG.fatal(fatalMsg, internal);
            }
            if(t instanceof RuntimeException) {
                throw (RuntimeException)t;
            }
            throw new RuntimeException(t);
        }
    }
    
    protected void saveToCycle() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(this);
        cycle.setInjectedNode(this);
    }
    
    protected void doPageService(ServiceCycle cycle) {
        try {
            boolean service = true;
        	while(service) {
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
                    ProcessStatus ret = page.doPageRender(
                    		requestedSuffix, extension);
                    saveToCycle();
                    SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
                    SpecificationUtil.endScope();
                    Response response = CycleUtil.getResponse();
                    if(ret == null) {
                        if(response.getWriter().isDirty() == false) {
                            throw new RenderNotCompletedException(
                                    pageName, extension);
                        }
                    }
                    response.flushAll();
                    service = false;
                } catch(PageForwarded f) {
                }
            }
        } catch(Throwable t) {
            cycle.getResponse().clearBuffer();
            SpecificationUtil.initScope();
            handleError(t);
        }
    }
    
    protected void doResourceService(ServiceCycle cycle) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        String path = cycle.getRequestScope().getRequestedPath();
        PageSourceDescriptor source = new PageSourceDescriptor();
        source.setSystemID(path);
        InputStream stream = source.getInputStream();
        if(stream != null) {
            OutputStream out = cycle.getResponse().getOutputStream();
            try {
                for(int i = stream.read(); i != -1; i = stream.read()) {
                    out.write(i);
                }
                out.flush();
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            cycle.getResponse().setStatus(404);
        }
    }
    
}

/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
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
        _paramNames.add(OUTPUT_WHITE_SPACE);
        _paramNames.add(SUFFIX_SEPARATOR);
        _paramNames.add(WELCOME_FILE_NAME);
    }

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
    
    private boolean match(Page page, String name, String extension) {
        String pageName = page.getPageName();
        String pageExt = page.getExtension();
        if(pageName.equals(name)) {
            if((StringUtil.isEmpty(pageExt) && StringUtil.isEmpty(extension)) ||
                    pageExt.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    public Page getPage(String pageName, String extension) {
        Page page;
        synchronized(this) {
            if(_pages != null) {
                for(Iterator it = new ChildSpecificationsIterator(_pages);
                        it.hasNext(); ) {
                    Object child = it.next();
                    if(child instanceof Page) {
                        page = (Page)child;
                        if(match(page, pageName, extension)) {
                            return page;
                        }
                    }
                }
            }
            page = new PageImpl(pageName, extension);
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            String path = pageName + ".maya";
            SourceDescriptor source = provider.getPageSourceDescriptor(path);
            page.setSource(source);
            if(_pages == null) {
                _pages = new ArrayList();
            }
            _pages.add(new SoftReference(page));
        }
        return page;
    }
   
	public void doService() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if(isPageRequested()) {
            prepareResponse(cycle.getResponse());
            doPageService(cycle);
        } else {
            doResourceService(cycle);
        }
	}

    protected boolean isPageRequested() {
        Request request = CycleUtil.getServiceCycle().getRequest();
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
            getErrorHandler().doErrorHandle(t);
            CycleUtil.getResponse().flush();
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
    
    protected void prepareResponse(Response response) {
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
    }
    
    private void saveToCycle() {
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
                    Object model = SpecificationUtil.getSpecificationModel(this);
                    SpecificationUtil.startScope(model, null);
                    SpecificationUtil.execEvent(this, QM_BEFORE_RENDER);
                    String pageName = cycle.getRequest().getPageName();
                    String extension = cycle.getRequest().getExtension();
                    Page page = getPage(pageName, extension);
                    ProcessStatus ret = null;
                    cycle.setPage(page);
                    ret = page.doPageRender();
                    cycle.setPage(null);
                    saveToCycle();
                    SpecificationUtil.execEvent(this, QM_AFTER_RENDER);
                    SpecificationUtil.endScope();
                    Response response = CycleUtil.getResponse();
                    if(ret == null) {
                        if(response.isFlushed() == false) {
                            throw new RenderNotCompletedException(
                                    pageName, extension);
                        }
                    }
                    response.flush();
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
        String path = cycle.getRequest().getRequestedPath();
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

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.MayaException;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.ScriptUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
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

    private static Set _paramNames;
    static {
        _paramNames = new HashSet();
        _paramNames.add(CHECK_TIMESTAMP);
        _paramNames.add(OUTPUT_WHITE_SPACE);
        _paramNames.add(REPORT_UNRESOLVED_ID);
        _paramNames.add(SUFFIX_SEPARATOR);
        _paramNames.add(WELCOME_FILE_NAME);
    }

    private Map _parameters;
    private ErrorHandler _errorHandler;
    
    public EngineImpl() {
        super(QM_ENGINE, null);
    }
    
    public void setParameter(String name, String value) {
        if(_paramNames.contains(name)) {
            if(_parameters == null) {
                _parameters = new HashMap();
            }
            if(StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(name);
            }
            _parameters.put(name, value);
        } else {
            throw new UnsupportedParameterException(name);
        }
    }

    public String getParameter(String name) {
        if(_paramNames.contains(name)) {
            if(_parameters == null) {
                return null;
            }
            return (String)_parameters.get(name);
        }
        throw new UnsupportedParameterException(name);
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
    
    public String getKey() {
        return "/engine";
    }
    
    public synchronized Page getPage(String pageName, String extension) {
        String key = SpecificationUtil.createPageKey(pageName, extension);
        Page page = SpecificationUtil.getPage(this, key);
        if(page == null) {
            String path = pageName + ".maya";
            page = new PageImpl(this, pageName, extension);
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            SourceDescriptor source = provider.getPageSourceDescriptor(path);
            page.setSource(source);
            addChildSpecification(page);
        }
        return page;
    }
   
	public void doService() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setCurrentNode(this);
        String path = cycle.getRequest().getRequestedPath();
        String mimeType = cycle.getApplication().getMimeType(path);
        if(mimeType != null && (
                mimeType.startsWith("text/html") || 
                mimeType.startsWith("text/xhtml")))  {
            prepareResponse(cycle.getResponse());
            doPageService(cycle);
        } else {
            doResourceService(cycle);
        }
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
        t.printStackTrace();
        getErrorHandler().doErrorHandle(t);
    }
    
    protected void prepareResponse(Response response) {
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
    }
    
    protected void doPageService(ServiceCycle cycle) {
        try {
            ScriptUtil.execEvent(this, QM_BEFORE_RENDER);
            String pageName = cycle.getRequest().getPageName();
            String extension = cycle.getRequest().getExtension();
            Page page = getPage(pageName, extension);
            page.doPageRender();
            cycle.setCurrentNode(this);
            ScriptUtil.execEvent(this, QM_AFTER_RENDER);
        } catch(Throwable t) {
            if(t instanceof MayaException) {
                ((MayaException)t).setCurrentNode(cycle.getCurrentNode());
            }
            cycle.getResponse().clearBuffer();
            cycle.resetPageScope();
            handleError(t);
        }
        cycle.getResponse().flush();
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
            OutputStream out = cycle.getResponse().getUnderlyingOutputStream();
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

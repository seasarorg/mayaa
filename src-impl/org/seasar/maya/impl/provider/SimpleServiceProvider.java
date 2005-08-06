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
package org.seasar.maya.impl.provider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.builder.SpecificationBuilder;
import org.seasar.maya.builder.TemplateBuilder;
import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.Response;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.el.ExpressionFactory;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.web.WebApplication;
import org.seasar.maya.impl.cycle.web.WebRequest;
import org.seasar.maya.impl.cycle.web.WebResponse;
import org.seasar.maya.impl.cycle.web.WebServiceCycle;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SimpleServiceProvider implements ServiceProvider, CONST_IMPL {
    
    private ServletContext _servletContext;
    private Application _application;
    private Engine _engine;
    private SourceFactory _sourceFactory;
    private ExpressionFactory _expressionFactory;
    private SpecificationBuilder _specificationBuilder;
    private TemplateBuilder _templateBuilder;
    private ServiceCyclePool _serviceCyclePool;
    private ThreadLocal _httpRequest;
    private ThreadLocal _httpResponse;
	
    public SimpleServiceProvider(ServletContext servletContext) {
        if(servletContext == null) {
            throw new IllegalArgumentException();
        }
        _servletContext = servletContext;
        _serviceCyclePool = new ServiceCyclePool();
        _httpRequest = new ThreadLocal();
        _httpResponse = new ThreadLocal();
    }
    
    public ServletContext getServletContext() {
        return _servletContext;
    }
    
    public Application getApplication() {
        if(_application == null) {
            WebApplication application = new WebApplication();
            application.setServletContext(_servletContext);
            _application = application;
        }
        return _application;
    }
    
    public boolean hasSourceFactory() {
        return _sourceFactory != null;
    }
    
    public void setSourceFactory(SourceFactory sourceFactory) {
        if(sourceFactory == null) {
            throw new IllegalArgumentException();
        }
        _sourceFactory = sourceFactory;
    }
    
    public SourceFactory getSourceFactory() {
        if(_sourceFactory == null) {
            throw new IllegalStateException();
        }
    	return _sourceFactory;
    }
    
    public boolean hasEngine() {
        return _engine != null;
    }
    
    public void setEngine(Engine engine) {
        if(engine == null) {
            throw new IllegalArgumentException();
        }
        _engine = engine;
    }
    
    public Engine getEngine() {
    	if(_engine == null) {
    	    throw new IllegalStateException();
    	}
        return _engine;
    }
    
    public boolean hasExpressionFactory() {
        return _expressionFactory != null;
    }
    
    public void setExpressionFactory(ExpressionFactory expressionFactory) {
        if(expressionFactory == null) {
            throw new IllegalArgumentException();
        }
        _expressionFactory = expressionFactory;
    }
    
    public ExpressionFactory getExpressionFactory() {
        if(_expressionFactory == null) {
            throw new IllegalStateException();
        }
        return _expressionFactory;
    }
    
    public boolean hasSpecificationBuilder() {
        return _specificationBuilder != null;
    }
    
    public void setSpecificationBuilder(SpecificationBuilder specificationBuilder) {
        if(specificationBuilder == null) {
            throw new IllegalArgumentException();
        }
        _specificationBuilder = specificationBuilder;
    }
    
    public SpecificationBuilder getSpecificationBuilder() {
    	if(_specificationBuilder == null) {
            throw new IllegalStateException();
    	}
        return _specificationBuilder;
    }
    
    public boolean hasTemplateBuilder() {
        return _templateBuilder != null;
    }
    
    public void setTemplateBuilder(TemplateBuilder templateBuilder) {
        if(templateBuilder == null) {
            throw new IllegalArgumentException();
        }
        _templateBuilder = templateBuilder;
    }
    
    public TemplateBuilder getTemplateBuilder() {
    	if(_templateBuilder == null) {
    	    throw new IllegalStateException();
    	}
        return _templateBuilder;
    }

	public void setHttpServletRequest(HttpServletRequest request) {
		if(request == null) {
			throw new IllegalArgumentException();
		}
		_httpRequest.set(request);
	}

	public void setHttpServletResponse(HttpServletResponse response) {
		if(response == null) {
			throw new IllegalArgumentException();
		}
		_httpResponse.set(response);
	}

	public ServiceCycle getServiceCycle() {
        return _serviceCyclePool.borrowServiceCycle();
    }
    
    public void releaseServiceCycle(ServiceCycle cycle) {
        _serviceCyclePool.returnServiceCycle(cycle);
    }
    
	public Object getModel(Object modelKey, String modelScope) {
        if(modelKey instanceof Class == false) {
            throw new IllegalArgumentException();
        }
        Class modelClass = (Class)modelKey;
        String modelName = modelClass.getName();
        ServiceCycle cycle = getServiceCycle(); 
        AttributeScope attrScope = cycle.getAttributeScope(modelScope);
        Object model = attrScope.getAttribute(modelName); 
        if(model != null) {
            return model;
        }
        model = ObjectUtil.newInstance(modelClass);
        attrScope.setAttribute(modelName, model);
        return model;
	}
    
    private class ServiceCyclePool extends AbstractSoftReferencePool {
    	
    	private ThreadLocal _current = new ThreadLocal();
        
        protected Request createRequest() {
        	HttpServletRequest request = (HttpServletRequest)_httpRequest.get();
        	if(request == null) {
        		throw new IllegalStateException();
        	}
            String suffixSeparator = _engine.getEngineSetting().getSuffixSeparator();
        	WebRequest webRequest = new WebRequest(suffixSeparator);
        	webRequest.setHttpServletRequest(request);
        	return webRequest;
        }
        
        protected Response createResponse() {
        	HttpServletResponse response = (HttpServletResponse)_httpResponse.get();
        	if(response == null) {
        		throw new IllegalStateException();
        	}
        	WebResponse webResponse = new WebResponse();
        	webResponse.setHttpServletResponse(response);
        	return webResponse;
        }

        protected Object createObject() {
            return new WebServiceCycle(getApplication());
        }
        
        protected boolean validateObject(Object object) {
            return object instanceof WebServiceCycle;
        }
    
        WebServiceCycle borrowServiceCycle() {
        	WebServiceCycle cycle = (WebServiceCycle)_current.get();
        	if(cycle == null) {
        		cycle = (WebServiceCycle)borrowObject();
                cycle.setRequest(createRequest());
                cycle.setResponse(createResponse());
                SpecificationUtil.setEngine(cycle, getEngine());
        		_current.set(cycle);
        	}
        	return cycle;
        }
        
        void returnServiceCycle(ServiceCycle cycle) {
            returnObject(cycle);
            _current.set(null);
        }
        
    }

}


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
package org.seasar.maya.impl.util;

import java.util.Iterator;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineUtil implements CONST_IMPL {

    private EngineUtil() {
    }

    public static String createTemplateKey(String suffix) {
        if(suffix == null) {
            throw new IllegalArgumentException();
        }
        return "/template[@suffix='" + suffix + "']";
    } 
    
    public static Template getTemplate(ServiceCycle cycle) {
        Template template = 
            (Template)cycle.getAttribute(KEY_TEMPLATE);
        if(template == null) {
            throw new IllegalStateException();
        }
        return template;
    }

    public static void setTemplate(ServiceCycle cycle, Template template) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(KEY_TEMPLATE, template);
    }
    
    public static String createPageKey(String pageName, String extension) {
        if(StringUtil.isEmpty(pageName)) {
            throw new IllegalArgumentException();
        }
        StringBuffer key = new StringBuffer();
        key.append("/page[@pageName='").append(pageName).append("']");
        if(StringUtil.hasValue(extension)) {
        	key.append("[@extension='").append(extension).append("']");
        }
        return key.toString();
    }
    
    public static Page getPage(Engine engine, String key) {
        for(Iterator it = engine.iterateChildSpecification(); it.hasNext(); ) {
            Page page = (Page)it.next();
            if(key.equals(page.getKey())) {
                return page;
            }
        }
        return null;
    }
    
    public static Page getPage(ServiceCycle cycle) {
        Page page = (Page)cycle.getAttribute(KEY_PAGE);
        if(page == null) {
            throw new IllegalStateException();
        }
        return page;
    }

    public static void setPage(ServiceCycle cycle, Page page) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(KEY_PAGE, page);
    }

    public static Engine getEngine(Specification specification) {
        if(specification instanceof Template) {
            return ((Template)specification).getPage().getEngine();
        } else if(specification instanceof Page) {
            return ((Page)specification).getEngine();
        } else if(specification instanceof Engine) {
            return (Engine)specification;
        }
        throw new IllegalArgumentException();
    }
    
    public static Engine getEngine(ServiceCycle cycle) {
        Engine engine = (Engine)cycle.getAttribute(KEY_ENGINE);
        if(engine == null) {
            throw new IllegalStateException();
        }
        return engine;
    }
    
    public static void setEngine(ServiceCycle cycle, Engine engine) {
        if(cycle == null || engine == null) {
            throw new IllegalArgumentException();
        }
        cycle.setAttribute(KEY_ENGINE, engine);
    }
    
    public static String getMimeType(Page page) {
        String extension = page.getPageName() + "." + page.getExtension();
        String ret = null ;
        if(StringUtil.hasValue(extension)) {
            ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
	        Application application = provider.getApplication();
	        ret = application.getMimeType(extension);
        }
        if( ret == null ) ret = "text/html" ;
        return ret ;
    }
    
}

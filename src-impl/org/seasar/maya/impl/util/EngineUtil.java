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
package org.seasar.maya.impl.util;

import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.jsp.PageContext;

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
    
    public static Template getTemplate(PageContext context) {
        Template template = 
            (Template)context.getAttribute(KEY_TEMPLATE);
        if(template == null) {
            throw new IllegalStateException();
        }
        return template;
    }

    public static void setTemplate(PageContext context, Template template) {
        if(context == null || template == null) {
            throw new IllegalArgumentException();
        }
        context.setAttribute(KEY_TEMPLATE, template);
    }
    
    public static void removeTemplate(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        context.removeAttribute(KEY_TEMPLATE);
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
    
    public static Page getPage(PageContext context) {
        Page page = (Page)context.getAttribute(KEY_PAGE);
        if(page == null) {
            throw new IllegalStateException();
        }
        return page;
    }

    public static void setPage(PageContext context, Page page) {
        if(context == null || page == null) {
            throw new IllegalArgumentException();
        }
        context.setAttribute(KEY_PAGE, page);
    }
    
    public static void removePage(PageContext context) {
        if(context == null) {
            throw new IllegalArgumentException();
        }
        context.removeAttribute(KEY_PAGE);
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
    
    public static Engine getEngine(PageContext context) {
        Engine engine = (Engine)context.getAttribute(KEY_ENGINE);
        if(engine == null) {
            throw new IllegalStateException();
        }
        return engine;
    }
    
    public static void setEngine(PageContext context, Engine engine) {
        if(context == null || engine == null) {
            throw new IllegalArgumentException();
        }
        context.setAttribute(KEY_ENGINE, engine);
    }
    
    public static String getMimeType(Page page) {
        String extension = page.getPageName() + "." + page.getExtension();
        String ret = null ;
        if(StringUtil.hasValue(extension)) {
            ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
	        ServletContext context = provider.getServletContext();
	        ret = context.getMimeType(extension);
        }
        if( ret == null ) ret = "text/html" ;
        return ret ;
    }
    
}

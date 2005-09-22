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
package org.seasar.maya.impl.engine.error;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.PageNotFoundException;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateErrorHandler  implements ErrorHandler {

    private static final Log LOG = LogFactory.getLog(TemplateErrorHandler.class);
    public static final String THROWABLE = "THROWABLE";

    private String _folder = "/"; 
    private String _extension = "html";
    
    private String getPageName(Class throwableClass) {
        String name = throwableClass.getName();
    	return StringUtil.preparePath(_folder) + StringUtil.preparePath(name);
    }
    
    public void setParameter(String name, String value) {
        if(StringUtil.isEmpty(value)) {
            throw new IllegalParameterValueException(getClass(), name);
        }
        if("folder".equals(name)) {
            _folder = value;
        } else if("extension".equals(name)) {
            _extension = value;
        } else {
            throw new UnsupportedParameterException(getClass(), name);
        }
    }
    
    public void doErrorHandle(Throwable t) {
        if(t == null) {
            throw new IllegalArgumentException();
        }
        CycleUtil.setAttribute(THROWABLE, t, ServiceCycle.SCOPE_REQUEST);
        try {
            for(Class throwableClass = t.getClass(); 
            		throwableClass != null; 
            		throwableClass = throwableClass.getSuperclass()) {
                String pageName = getPageName(throwableClass);
                try {
                    Engine engine = EngineUtil.getEngine();
                	Page page = engine.getPage(pageName);
                    page.doPageRender("", _extension);
                    if(LOG.isErrorEnabled()) {
                        String msg = StringUtil.getMessage(
                                TemplateErrorHandler.class, 1, 
                                new String[] { t.getMessage() });
                        LOG.error(msg, t);
                    }
    	            break;
                } catch(PageNotFoundException ignore) {
                    if(LOG.isInfoEnabled()) {
                        String msg = StringUtil.getMessage(
                                TemplateErrorHandler.class, 2, 
                                new String[] { pageName });
                        LOG.info(msg);
                    }
                }
            }
        } finally {
            CycleUtil.removeAttribute(THROWABLE, ServiceCycle.SCOPE_REQUEST);
        }
    }
    
}

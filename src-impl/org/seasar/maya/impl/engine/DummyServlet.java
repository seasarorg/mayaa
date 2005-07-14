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
package org.seasar.maya.impl.engine;

import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.seasar.maya.impl.util.collection.NullEnumeration;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class DummyServlet implements Servlet {

    private DummyServletConfig _servletConfig;
    
    public DummyServlet(ServletContext servletContext) {
        _servletConfig = new DummyServletConfig(servletContext);
    }
    
    public void destroy() {
    }
    
    public ServletConfig getServletConfig() {
        return _servletConfig;
    }
    
    public String getServletInfo() {
        return null;
    }
    
    public void init(ServletConfig config) {
    }

    public void service(ServletRequest request, ServletResponse response) {
    }

    private class DummyServletConfig implements ServletConfig {

        private ServletContext _servletContext;
        
        private DummyServletConfig(ServletContext servletContext) {
            _servletContext = servletContext;
        }
        
    	public String getInitParameter(String name) {
            return null;
        }
        
    	public Enumeration getInitParameterNames() {
            return NullEnumeration.getInstance();
        }
        
    	public ServletContext getServletContext() {
            return _servletContext;
        }
        
    	public String getServletName() {
            return "Internal dummy servlet";
        }
    
    }
    
}

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
package org.seasar.maya.test.util.servlet.jsp;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspEngineInfo;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestJspFactory extends JspFactory {
    
    private JspEngineInfo _engineInfo;
    private PageContextPool pageContextPool = new PageContextPool(); 
    
    public JspEngineInfo getEngineInfo() {
    	if(_engineInfo == null) {
    		_engineInfo = new TestJspEngineInfo();
    	}
        return _engineInfo;
    }

    public PageContext getPageContext(Servlet servlet, ServletRequest request,
            ServletResponse response, String errorPageURL,
            boolean needsSession, int bufferSize, boolean autoFlush) {
		PageContext pageContext = pageContextPool.borrowPageContext();
		try {
			pageContext.initialize(servlet, request, response, 
			        errorPageURL, needsSession, bufferSize, autoFlush);
		} catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
		    releasePageContext(pageContext);
		}
		return pageContext;
    }

    public void releasePageContext(PageContext pageContext) {
    	pageContext.release();
   		pageContextPool.returnPageContext(pageContext);
    }
    
    private class PageContextPool extends AbstractSoftReferencePool {
        
    	public Object createObject() {
			return new TestPageContext();
		}
    	
		public boolean validateObject(Object object) {
			return object instanceof PageContext;
		}
		
		public PageContext borrowPageContext() {
		    return (PageContext)borrowObject();
		}
		
		public void returnPageContext(PageContext pageContext) {
		    returnObject(pageContext);
		}
    
    }

}

/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.error;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.engine.error.ErrorHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class JspContextErrorHandler  implements ErrorHandler {

    private static final Log LOG = LogFactory.getLog(JspContextErrorHandler.class);
    
    public void putParameter(String name, String value) {
    }
    
    public void doErrorHandle(PageContext context, Throwable t) throws Throwable {
        if(t == null || context == null) {
            throw new IllegalArgumentException();
        }
        context.handlePageException(t);
        if(LOG.isTraceEnabled()) {
            LOG.trace(t);
        }
    }
    
}

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
package org.seasar.maya.test.util;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.PageContext;

import org.seasar.maya.impl.engine.DummyServlet;
import org.seasar.maya.test.util.servlet.jsp.TestPageContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TestUtil {

    public static PageContext createPageContext(ServletContext servletContext, 
            ServletRequest request, ServletResponse response) throws IOException {
        TestPageContext pageContext = new TestPageContext();
        pageContext.initialize(new DummyServlet(), request, response, null, true, 1024 * 8, true);
        return pageContext;
    }
    
}

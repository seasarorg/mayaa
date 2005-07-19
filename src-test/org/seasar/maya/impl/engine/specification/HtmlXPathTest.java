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
package org.seasar.maya.impl.engine.specification;

import junit.framework.TestCase;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPathFunctionContext;
import org.jaxen.pattern.Pattern;
import org.jaxen.pattern.PatternParser;
import org.jaxen.saxpath.SAXPathException;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.xml.NullLocator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class HtmlXPathTest extends TestCase implements CONST_IMPL {
    
    public void testHtmlMatches() throws SAXPathException {
        Pattern pattern = null;
        ContextSupport support = new ContextSupport(
                new SimpleNamespaceContext(),
                XPathFunctionContext.getInstance(),
                new SimpleVariableContext(),
                SpecificationNavigator.getInstance());
        Context context = new Context(support);
        pattern = PatternParser.parse("@class='box'");
        
        SpecificationNodeImpl node = new SpecificationNodeImpl(QH_HTML, NullLocator.getInstance());
        node.addAttribute(new QName(URI_HTML, "class"), "box");
        
        assertTrue(pattern.matches(node, context));
    }
    
}

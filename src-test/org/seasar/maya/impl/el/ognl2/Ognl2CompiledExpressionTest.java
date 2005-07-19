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
package org.seasar.maya.impl.el.ognl2;

import junit.framework.TestCase;

import org.seasar.maya.impl.el.resolver.CompositeExpressionResolver;
import org.seasar.maya.impl.el.resolver.ImplicitObjectExpressionResolver;
import org.seasar.maya.impl.el.resolver.ScopedAttributeExpressionResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class Ognl2CompiledExpressionTest extends TestCase {

//    private Ognl2ExpressionFactory _factory;
    
    public Ognl2CompiledExpressionTest(String test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        CompositeExpressionResolver resolver = new CompositeExpressionResolver();
        resolver.add(new ScopedAttributeExpressionResolver());
        resolver.add(new ImplicitObjectExpressionResolver()); 
//        _factory = new Ognl2ExpressionFactory();
//        _pageContext = new TestPageContext();
//        _pageContext.initialize(
//                new DummyServlet(), request, response, null, true, 10000, true);
    }
    
//    public void testGetValue() {
//        CompiledExpression exp = _factory.createExpression("${ helloModel.greeting }", String.class);
//        Map helloModel = new HashMap();
//        helloModel.put("greeting", "hello");
//        request.setAttribute("helloModel", helloModel);
//        String val = (String)exp.getValue(_pageContext);
//        assertEquals("hello", val);
//    }
//    
//    public void testCallMethod() {
//        CompiledExpression exp = _factory.createExpression("${ helloModel.get('greeting') }", String.class);
//        Map helloModel = new HashMap();
//        helloModel.put("greeting", "hello");
//        request.setAttribute("helloModel", helloModel);
//        String val = (String)exp.getValue(_pageContext);
//        assertEquals("hello", val);
//    }
    
    public void testCallMethod2() {
//        CompiledExpression exp = _factory.createExpression("${ new java.util.Date() }", Date.class);
//        Object val = exp.getValue(_pageContext);
//        assertNotNull(val);
//        assertTrue(val instanceof Date);
    }
    
}

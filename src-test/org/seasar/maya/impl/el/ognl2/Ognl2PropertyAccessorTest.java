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
import ognl.OgnlContext;

import org.seasar.maya.impl.el.resolver.CompositeExpressionResolver;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class Ognl2PropertyAccessorTest extends TestCase {

    private Ognl2PropertyAccessor _accessor;
    private String _sample;
    
    public Ognl2PropertyAccessorTest(String test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        CompositeExpressionResolver resolver = new CompositeExpressionResolver();
        _accessor = new Ognl2PropertyAccessor(resolver);
    }
    
    public String getSample() {
        return "test string";
    }
    
    public void setSample(String sample) {
        _sample = sample;
    }
    
    public void testGetValue() {
        Object ret = _accessor.getProperty(new OgnlContext(), this, "sample");
        assertEquals("test string", ret);
    }
    
    public void testSetValue() {
        _accessor.setProperty(new OgnlContext(), this, "sample", "set value");
        assertEquals("set value", _sample);
    }
    
}

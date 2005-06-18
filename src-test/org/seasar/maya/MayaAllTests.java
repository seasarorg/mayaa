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
package org.seasar.maya;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.seasar.maya.engine.processor.TemplateProcessorSupportTest;
import org.seasar.maya.impl.builder.library.scanner.MLDHanderTest;
import org.seasar.maya.impl.builder.library.scanner.MLDLibraryScannerTest;
import org.seasar.maya.impl.el.ExpressionBlockIteratorTest;
import org.seasar.maya.impl.el.ExpressionBlockTest;
import org.seasar.maya.impl.el.ognl2.Ognl2CompiledExpressionTest;
import org.seasar.maya.impl.el.ognl2.Ognl2PropertyAccessorTest;
import org.seasar.maya.impl.engine.EngineImplTest;
import org.seasar.maya.impl.engine.PageImplTest;
import org.seasar.maya.impl.engine.TemplateImplTest;
import org.seasar.maya.impl.engine.specification.HtmlXPathTest;
import org.seasar.maya.impl.engine.specification.NamespaceableImplTest;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImplTest;
import org.seasar.maya.impl.engine.specification.SpecificationXPathTest;
import org.seasar.maya.impl.source.JavaSourceDescriptorTest;
import org.seasar.maya.impl.source.MetaInfCacheTest;
import org.seasar.maya.impl.source.MetaInfSourceDescriptorTest;
import org.seasar.maya.standard.builder.library.scanner.TLDLibraryScannerTest;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaAllTests extends TestSuite {

    public static Test suite ( ) {
        TestSuite suite = new TestSuite("Maya All Tests");
        
        // org.seasar.maya.engine.processor
        suite.addTestSuite(TemplateProcessorSupportTest.class);

        // org.seasar.maya.impl.builder.library.scanner
        suite.addTestSuite(MLDHanderTest.class);
        
        // org.seasar.maya.impl.builder.library
        suite.addTestSuite(MLDLibraryScannerTest.class);
        
        // org.seasar.maya.impl.el
        suite.addTestSuite(ExpressionBlockTest.class);
        suite.addTestSuite(ExpressionBlockIteratorTest.class);
        
        // org.seasar.maya.impl.el.ognl2
        suite.addTestSuite(Ognl2PropertyAccessorTest.class);
        suite.addTestSuite(Ognl2CompiledExpressionTest.class);
        
        // org.seasar.maya.impl.engine
        suite.addTestSuite(EngineImplTest.class);
        suite.addTestSuite(PageImplTest.class);
        suite.addTestSuite(TemplateImplTest.class);

        // org.seasar.maya.impl.source
        suite.addTestSuite(JavaSourceDescriptorTest.class);
        suite.addTestSuite(MetaInfCacheTest.class);
        suite.addTestSuite(MetaInfSourceDescriptorTest.class);
        
        // org.seasar.maya.impl.engine.specification
        suite.addTestSuite(HtmlXPathTest.class);
        suite.addTestSuite(NamespaceableImplTest.class);
        suite.addTestSuite(SpecificationNodeImplTest.class);
        suite.addTestSuite(SpecificationXPathTest.class);
        
        // org.seasar.maya.standard.builder.library.scanner
        suite.addTestSuite(TLDLibraryScannerTest.class);
        
        return suite;
    }

}

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
package org.seasar.maya.impl.jsp.builder.library.scanner;

import java.util.Iterator;

import junit.framework.TestCase;

import org.seasar.maya.impl.jsp.builder.library.JspLibraryDefinition;
import org.seasar.maya.impl.jsp.builder.library.JspProcessorDefinition;
import org.seasar.maya.impl.jsp.builder.library.scanner.TLDLibraryScanner;
import org.seasar.maya.impl.source.MetaInfSourceDescriptor;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDLibraryScannerTest extends TestCase {

    public TLDLibraryScannerTest(String name) {
        super(name);
    }
    
    public void testParseTLD() {
        System.out.println("start--------------");
    	SourceDescriptor metaInf = new MetaInfSourceDescriptor("/");
        for(Iterator it = metaInf.iterateChildren("tld"); it.hasNext(); ) {
	        SourceDescriptor source = (SourceDescriptor)it.next();
	        TLDLibraryScanner scanner = new TLDLibraryScanner();
            System.out.println(source.getSystemID() + "------------------");
            JspLibraryDefinition library = scanner.parseTLD(
                    source.getInputStream(), source.getSystemID());
            assertNotNull(library);
            for(Iterator it2 = library.iterateProcessorDefinition(); it2.hasNext(); ) {
                JspProcessorDefinition processor = (JspProcessorDefinition)it2.next();
                System.out.println(processor.getName() + ": " + processor.getClassName());
            }
        }
        System.out.println("end--------------");
    }
    
}

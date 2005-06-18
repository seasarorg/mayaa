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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.Iterator;

import junit.framework.TestCase;

import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.impl.source.JavaSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MLDLibraryScannerTest extends TestCase {

    public MLDLibraryScannerTest(String name) {
        super(name);
    }
    
    public void testParseMLD() {
        MLDLibraryScanner scanner = new MLDLibraryScanner();
        JavaSourceDescriptor source = new JavaSourceDescriptor(
                "TestMLD.mld", MLDLibraryScannerTest.class);
        LibraryDefinition library = scanner.parseMLD(source.getInputStream(), source.getPath());
        assertNotNull(library);
        assertEquals("URI_LIBRARY", library.getNamespaceURI());
        Iterator it = library.iterateProcessorDefinition();
        ProcessorDefinition processor = (ProcessorDefinition)it.next();
        assertNotNull(processor);
        assertFalse(it.hasNext());
        
        assertEquals("NAME_PROCESSOR", processor.getName());
        assertEquals("org.seasar.maya.engine.processor.TemplateProcessorSupport",
        		processor.getClassName());
        it = processor.iteratePropertyDefinition();
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertFalse(it.hasNext());
    }
}

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
package org.seasar.maya.impl.source;

import junit.framework.TestCase;

import org.seasar.maya.builder.library.scanner.LibraryScanner;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JavaSourceDescriptorTest extends TestCase {

    public JavaSourceDescriptorTest(String name) {
        super(name);
    }
    
    public void testPrepareLoadedSource() {
        // xercesImpl.jar‚É‚Í‚¢‚Á‚Ä‚é
        JavaSourceDescriptor source = 
            new JavaSourceDescriptor("commons-logging.properties");
        source.prepareLoadedSource();
        assertTrue(source.exists());
    }

    public void testPrepareLoadedSource2() {
        JavaSourceDescriptor source = new JavaSourceDescriptor(
                "mld_1_0.dtd", LibraryScanner.class);
        source.prepareLoadedSource();
        assertTrue(source.exists());
    }
    
}

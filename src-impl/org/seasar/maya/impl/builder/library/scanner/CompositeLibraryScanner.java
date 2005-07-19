/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.builder.library.scanner;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.scanner.LibraryScanner;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeLibraryScanner implements LibraryScanner {

    private List _scanner = new ArrayList();
    
    public void add(LibraryScanner scanner) {
        if(scanner == null) {
            throw new IllegalArgumentException();
        }
        _scanner.add(scanner);
    }
    
    public void scanLibrary(LibraryManager manager) {
        for(Iterator it = _scanner.iterator(); it.hasNext(); ) {
            LibraryScanner scanner = (LibraryScanner)it.next();
            scanner.scanLibrary(manager);
        }
    }

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}

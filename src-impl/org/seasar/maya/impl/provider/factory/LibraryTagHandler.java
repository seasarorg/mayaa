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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.impl.builder.library.LibraryManagerImpl;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryTagHandler extends AbstractParameterizableTagHandler {
    
    private TemplateBuilderTagHandler _parent;
    private LibraryManagerImpl _libraryManager;
    
    public LibraryTagHandler(TemplateBuilderTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler("scanner", new LibraryScannerTagHandler(this));
    }
    
    public void start(Attributes attributes) {
        _libraryManager = new LibraryManagerImpl();
        _parent.getTemplateBuilder().setLibraryManager(_libraryManager);
    }
    
    public void end(String body) {
        _libraryManager = null;
    }
    
    public LibraryManagerImpl getLibraryManager() {
        if(_libraryManager == null) {
            throw new IllegalStateException();
        }
        return _libraryManager;
    }
    
    public Parameterizable getParameterizable() {
        return getLibraryManager();
    }

}

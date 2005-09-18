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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryManagerTagHandler extends AbstractParameterizableTagHandler {
    
    private ServiceTagHandler _parent;
    private LibraryManager _libraryManager;
    
    public LibraryManagerTagHandler(ServiceTagHandler parent) {
        super("libraryManager");
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler(new SourceTagHandler(this));
        putHandler(new BuilderTagHandler(this));
    }
    
    public void start(Attributes attributes) {
    	_libraryManager = (LibraryManager)XMLUtil.getObjectValue(
                attributes, "class", null, LibraryManager.class);
        _parent.getServiceProvider().setLibraryManager(_libraryManager);
    }
    
    public void end(String body) {
        _libraryManager = null;
    }
    
    public LibraryManager getLibraryManager() {
        if(_libraryManager == null) {
            throw new IllegalStateException();
        }
        return _libraryManager;
    }
    
    public Parameterizable getParameterizable() {
        return getLibraryManager();
    }

}

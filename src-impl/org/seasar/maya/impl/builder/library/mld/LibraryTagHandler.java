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
package org.seasar.maya.impl.builder.library.mld;

import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryTagHandler extends TagHandler {

    private LibraryDefinitionImpl _libraryDefinition;
    
    public LibraryTagHandler() {
        super("library");
        putHandler( new ProcessorTagHandler(this));
    }
    
    protected void start(Attributes attributes) {
        _libraryDefinition = new LibraryDefinitionImpl();
		String uri = attributes.getValue("uri");
		_libraryDefinition = new LibraryDefinitionImpl();
		_libraryDefinition.setNamespaceURI(uri);
    }
    
    public LibraryDefinitionImpl getLibraryDefinition() {
        return _libraryDefinition;
    }
    
}

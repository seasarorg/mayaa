/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.library.mld;

import org.seasar.maya.ParameterAware;
import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LibraryTagHandler 
		extends AbstractParameterizableTagHandler {

    private LibraryDefinitionImpl _libraryDefinition;
    
    public LibraryTagHandler() {
        super("library");
        putHandler(new ConverterTagHandler(this, this));
        putHandler( new ProcessorTagHandler(this));
        putHandler(new PropertySetTagHandler("propertySet", this, this));
    }
    
    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _libraryDefinition = new LibraryDefinitionImpl();
		String uri = attributes.getValue("uri");
		_libraryDefinition = new LibraryDefinitionImpl();
		_libraryDefinition.setNamespaceURI(uri);
		_libraryDefinition.setSystemID(systemID);
    }
    
    public LibraryDefinitionImpl getLibraryDefinition() {
    	if(_libraryDefinition == null) {
    		throw new IllegalStateException();
    	}
        return _libraryDefinition;
    }

	public ParameterAware getParameterizable() {
		return getLibraryDefinition();
	}
    
}

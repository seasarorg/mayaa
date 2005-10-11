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
import org.seasar.maya.impl.builder.library.ProcessorDefinitionImpl;
import org.seasar.maya.impl.builder.library.PropertySetImpl;
import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PropertySetTagHandler 
        extends AbstractParameterizableTagHandler {

    private TagHandler _parent;
    private LibraryTagHandler _libraryTagHandler;
    private PropertySetImpl _propertySet;
    
    public PropertySetTagHandler(String name,
            TagHandler parent, LibraryTagHandler libraryTagHandler) {
        super(name);
        if(parent == null || libraryTagHandler == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        _libraryTagHandler = libraryTagHandler;
        putHandler(new PropertyTagHandler(this, libraryTagHandler));
    }

    protected PropertySetImpl createPropertySet() {
        return new PropertySetImpl();
    }
    
    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        String name = attributes.getValue("name");
        if(StringUtil.isEmpty(name)) {
            throw new IllegalStateException();
        }
        _propertySet = createPropertySet();
        _propertySet.setName(name);
        _propertySet.setLineNumber(lineNumber);
    }
    
    protected void addToLibrary(LibraryDefinitionImpl library) {
        if(_parent instanceof ProcessorTagHandler) {
            ProcessorDefinitionImpl processorDef = 
                ((ProcessorTagHandler)_parent).getProcessorDefinition();
            processorDef.addPropertySetName(_propertySet.getName());
        }
        if(_propertySet.iteratePropertyDefinition().hasNext()) {
            library.addPropertySet(_propertySet);
        }
    }

    protected void end(String body) {
        LibraryDefinitionImpl library = _libraryTagHandler.getLibraryDefinition();
        _propertySet.setLibraryDefinition(library);
        addToLibrary(library);
        _propertySet = null;
    }
    
    public PropertySetImpl getPropertySet() {
        if(_propertySet == null) {
            throw new IllegalStateException();
        }
        return _propertySet;
    }

    public ParameterAware getParameterizable() {
        throw new IllegalStateException();
    }

}

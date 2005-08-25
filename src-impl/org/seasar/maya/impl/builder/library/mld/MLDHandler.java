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

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.library.LibraryDefinitionImpl;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MLDHandler extends DefaultHandler implements CONST_IMPL {

    private TagHandlerStack _stack;    

    public LibraryDefinitionImpl getLibraryDefinition() {
        return ((LibraryTagHandler)_stack.getRoot()).getLibraryDefinition();
    }
    
    public MLDHandler() {
        _stack = new TagHandlerStack("library", new LibraryTagHandler());
    }
    
	public InputSource resolveEntity(String publicId, String systemId) {
        if(PUBLIC_MLD10.equals(publicId)) {
            ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor();
            source.setSystemID("mld_1_0.dtd");
            source.setNeighborClass(MLDHandler.class);
            if(source.exists()) {
                return new InputSource(source.getInputStream());
            }
        }
        return null;
    }
	
    public void startElement(String namespaceURI, 
            String localName, String qName, Attributes attr) {
        _stack.startElement(localName, attr);
    }
    
    public void endElement(String namespaceURI, String localName, String qName) {
        _stack.endElement();
    }
    
    public void fatalError(SAXParseException e) {
        error(e);
    }

    public void error(SAXParseException e) {
        throw new RuntimeException(e);
    }

}

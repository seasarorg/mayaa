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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(MLDHandler.class);
    
    private LibraryTagHandler _handler;
    private TagHandlerStack _stack;
    
    public MLDHandler() {
        _handler = new LibraryTagHandler();
        _stack = new TagHandlerStack(_handler);
    }

    public LibraryDefinitionImpl getLibraryDefinition() {
        return _handler.getLibraryDefinition();
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
    
    public void warning(SAXParseException e) {
        LOG.warn(e.getMessage(), e);
    }

    public void error(SAXParseException e) {
        if(LOG.isErrorEnabled()) {
            LOG.error(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }

    public void fatalError(SAXParseException e) {
        if(LOG.isFatalEnabled()) {
            LOG.fatal(e.getMessage(), e);
        }
        throw new RuntimeException(e);
    }

}

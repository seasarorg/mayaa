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
package org.seasar.maya.impl.builder.library.tld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.builder.library.TLDLibraryDefinition;
import org.seasar.maya.impl.builder.library.entity.J2EEEntityResolver;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Koji Suga (Gluegent, Inc.)
 * TODO util‚É‹¤’Ê‰»
 */
public class TLDHandler extends DefaultHandler {
    
    private static final Log LOG = LogFactory.getLog(TLDHandler.class);
	
    private TaglibTagHandler _handler;
	private TagHandlerStack _stack;
	private Locator _locator;
	
    public TLDHandler() {
        _handler = new TaglibTagHandler();
        _stack = new TagHandlerStack(_handler);
    }
    
    public TLDLibraryDefinition getLibraryDefinition() {
        return _handler.getLibraryDefinition();
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return J2EEEntityResolver.resolveEntity(publicId, systemId);
    }
    
	public void setDocumentLocator(Locator locator) {
		_locator = locator;
	}

	protected String getSystemID() {
		if(_locator != null) {
			return _locator.getSystemId();
		}
		return null;
	}

	protected int getLineNumber() {
		if(_locator != null) {
			return _locator.getLineNumber();
		}
		return 0;
	}

	public void startElement(String namespaceURI, 
			String localName, String qName, Attributes attributes) {
		_stack.startElement(
				localName, attributes, getSystemID(), getLineNumber());
	}

	public void endElement(String namespaceURI,
			String localName, String qName) {
        _stack.endElement();
	}

	public void characters(char[] ch, int start, int length) {
        _stack.characters(ch, start, length);
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

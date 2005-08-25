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
package org.seasar.maya.impl.builder.library.tld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.builder.library.JspLibraryDefinition;
import org.seasar.maya.impl.builder.library.entity.J2eeEntityResolver;
import org.seasar.maya.impl.util.xml.TagHandlerStack;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author suga
 */
public class TLDHandler extends DefaultHandler {
    
    private static final Log LOG = LogFactory.getLog(TLDHandler.class);
	
	private TagHandlerStack _stack;
    private TaglibTagHandler _handler;
	
    public TLDHandler() {
        _handler = new TaglibTagHandler();
        _stack = new TagHandlerStack(_handler);
    }
    
    public JspLibraryDefinition getLibraryDefinition() {
        return _handler.getLibraryDefinition();
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return J2eeEntityResolver.resolveEntity(publicId, systemId);
    }

	public void startElement(
			String namespaceURI, String localName, String qName, Attributes attributes) {
		_stack.startElement(localName, attributes);
	}

	public void endElement(String namespaceURI, String localName, String qName) {
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

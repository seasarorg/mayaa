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
package org.seasar.maya.impl.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.parser.AdditionalHandler;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.specification.NamespaceableImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeHandler
        implements EntityResolver, DTDHandler, ContentHandler, 
        ErrorHandler, LexicalHandler, AdditionalHandler, CONST_IMPL {
    
    private static final Log LOG = 
        LogFactory.getLog(SpecificationNodeHandler.class);

    private Specification _specification;
    private NodeTreeWalker _current;
    private Locator _locator;
    private Namespaceable _namespaces;
    private StringBuffer _charactersBuffer;
    private boolean _outputWhitespace;
    private int _inEntity;
    
    public SpecificationNodeHandler(Specification specification) {
        if(specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
    }

    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    private void initNamespaceScope() {
        _namespaces = new NamespaceableImpl();
        _namespaces.addNamespace("", URI_HTML);
    }

    private void stackNamespaceScope() {
        Namespaceable parentScope = _namespaces;
        _namespaces = new NamespaceableImpl();
        _namespaces.setParentScope(parentScope);
    }
    
    private void popNamespaceScope() {
        _namespaces = _namespaces.getParentScope();
        if(_namespaces == null) {
            throw new IllegalStateException();
        }
    }
    
    public void startDocument() {
        _charactersBuffer = new StringBuffer(128);
        _outputWhitespace = EngineUtil.getEngineSettingBoolean(
                OUTPUT_WHITE_SPACE, true);
        _current = _specification;
        initNamespaceScope();
        stackNamespaceScope();
    }

    public void startPrefixMapping(String prefix, String uri) {
        _namespaces.addNamespace(prefix, uri);
    }
    
    public void endPrefixMapping(String prefix) {
        NodeNamespace ns = _namespaces.getNamespace(prefix, false);
        if(ns == null) {
            throw new IllegalStateException();
        }
    }
    
    private SpecificationNode addNode(QName qName) {
		SpecificationNodeImpl child = new SpecificationNodeImpl(
				qName, _locator.getSystemId(), _locator.getLineNumber());
        child.setParentScope(_namespaces);
	    _current.addChildNode(child);
		return child;
    }
    
    private void addCharactersNode() {
    	if(_charactersBuffer.length() > 0) {
    		SpecificationNode node = addNode(QM_CHARACTERS);
    		node.addAttribute(QM_TEXT, _charactersBuffer.toString());
            _charactersBuffer = new StringBuffer(128);
    	}
    }
    
    private void saveToCycle(NodeTreeWalker originalNode) {
        if(originalNode instanceof SpecificationNode) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.setOriginalNode((SpecificationNode)originalNode);
        } else {
            throw new IllegalStateException();
        }
    }
    
    public void startElement(String namespaceUR, 
            String localName, String qName, Attributes attributes) {
        addCharactersNode();
        QNameable parsedName = 
            BuilderUtil.parseName(_namespaces, qName);
        QName nodeQName = parsedName.getQName();
        String nodeURI = nodeQName.getNamespaceURI();
        SpecificationNode node = addNode(nodeQName);
        Namespaceable elementNS = new NamespaceableImpl();
        elementNS.setParentScope(_namespaces);
        elementNS.addNamespace("", nodeURI);
        for(int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            QNameable parsedAttrName = 
                BuilderUtil.parseName(elementNS, attrName);
            QName attrQName = parsedAttrName.getQName();
            node.addAttribute(attrQName, attributes.getValue(i));
        }
        _current = node;
        saveToCycle(_current);
        stackNamespaceScope();
    }

    public void endElement(String namespaceURI, 
            String localName, String qName) {
        popNamespaceScope();
        addCharactersNode();
        _current = _current.getParentNode();
        saveToCycle(_current);
    }

    public void endDocument() {
        saveToCycle(_specification);
    }

    public void characters(char[] buffer, int start, int length) {
    	if(_inEntity == 0) {
	    	if(_outputWhitespace) {
	            _charactersBuffer.append(buffer, start, length);
	    	} else {
	    	    String characters = new String(buffer, start, length).trim();
		        if(characters.length() > 0) {
		            _charactersBuffer.append(characters);
		        }
	    	}
    	}
    }

    public void ignorableWhitespace(char[] buffer, int start, int length) {
    	if(_outputWhitespace) {
    		_charactersBuffer.append(buffer, start, length);
    	}
    }
    
    public void xmlDecl(String version, String encoding, String standalone) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PROCESSING_INSTRUCTION);
        node.addAttribute(QM_TARGET, "xml");
        StringBuffer buffer = new StringBuffer();
        if(StringUtil.hasValue(version)) {
            buffer.append("version=\"").append(version).append("\" ");
        }
        if(StringUtil.hasValue(encoding)) {
            buffer.append("encoding=\"").append(encoding).append("\" ");
        }
        if(StringUtil.hasValue(standalone)) {
            buffer.append("standalone=\"").append(standalone).append("\" ");
        }
        if(buffer.length() > 0) {
            node.addAttribute(QM_DATA, buffer.toString());
        }
    }

    public void processingInstruction(String target, String data) {
        addCharactersNode();
		SpecificationNode node = addNode(QM_PROCESSING_INSTRUCTION);
		node.addAttribute(QM_TARGET, target);
		if(StringUtil.hasValue(data)) {
			node.addAttribute(QM_DATA, data);
		}
    }

    public void skippedEntity(String name) {
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return null;
    }

    public void startEntity(String name) {
        String entityRef = "&" + name + ";";
        _charactersBuffer.append(entityRef);
        ++_inEntity;
    }

    public void endEntity(String name) {
        --_inEntity;
    }
    
    public void comment(char[] buffer, int start, int length) {
        addCharactersNode();
        String comment = new String(buffer, start, length);
        SpecificationNode node = addNode(QM_COMMENT);
        node.addAttribute(QM_TEXT, comment);
    }

    public void notationDecl(String name, String publicId, String systemId) {
    }

    public void unparsedEntityDecl(
            String name, String publicId, String systemId, String notationName) {
    }

    public void startDTD(String name, String publicID, String systemID) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_DOCTYPE);
		node.addAttribute(QM_NAME, name);
		if(StringUtil.hasValue(publicID)) {
			node.addAttribute(QM_PUBLIC_ID, publicID);
		}
		if(StringUtil.hasValue(systemID)) {
			node.addAttribute(QM_SYSTEM_ID, systemID);
		}
		_charactersBuffer.append("\r\n");
    }

    public void endDTD() {
    }

    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);
        _current = node; 
    }

    public void endCDATA() {
        addCharactersNode();
        _current = _current.getParentNode();
    }

    public void warning(SAXParseException e) {
        if(LOG.isWarnEnabled()) {
            LOG.warn(e);
        }
    }

    public void fatalError(SAXParseException e) {
        if(LOG.isFatalEnabled()) {
            LOG.fatal(e);
        }
        throw new RuntimeException(e);
    }
    
    public void error(SAXParseException e) {
        if(LOG.isErrorEnabled()) {
            LOG.error(e);
        }
        throw new RuntimeException(e);
    }

}

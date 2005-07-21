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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.NamespaceableImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.xml.NullLocator;
import org.seasar.maya.provider.EngineSetting;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationNodeHandler extends DefaultHandler
		implements LexicalHandler, CONST_IMPL {
    
    private static final Log LOG = LogFactory.getLog(SpecificationNodeHandler.class);

    private Specification _specification;
    private SpecificationNode _current;
    private Locator _locator;
    private NamespaceableImpl _namespaces;
    private StringBuffer _charactersBuffer;
    private boolean _outputWhitespace;
    private int _inEntity;
    
    public SpecificationNodeHandler(Specification specification) {
        if(specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
    }

    public void startDocument() {
        _charactersBuffer = new StringBuffer(128);
        _namespaces = new NamespaceableImpl();
        EngineSetting engineSetting = SpecificationUtil.getEngine(_specification).getEngineSetting();
        _outputWhitespace = engineSetting.isOutputWhitespace();
        _current = _specification;
        _locator = NullLocator.getInstance();
    }

    public void startPrefixMapping(String prefix, String uri) {
        _namespaces.addNamespace(prefix, uri);
    }
    
    public void endPrefixMapping(String prefix) {
        _namespaces.removeNamespace(prefix);
    }
    
    private SpecificationNode addNode(QName qName) {
		SpecificationNodeImpl child = new SpecificationNodeImpl(qName, _locator);
        for(Iterator it = _namespaces.iterateNamespace(); it.hasNext();) {
            NodeNamespace ns = (NodeNamespace)it.next();
            child.addNamespace(ns.getPrefix(), ns.getNamespaceURI());
        }
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
    
    public void startElement(
            String namespaceUR, String localNam, String qName, Attributes attributes) {
        addCharactersNode();
        NodeNamespace ns = _specification.getDefaultNamespace();
        QNameable parsedName = SpecificationUtil.parseName(
                _namespaces, _specification, _locator, qName, ns.getNamespaceURI());
        QName nodeQName = parsedName.getQName();
        String nodeURI = nodeQName.getNamespaceURI();
        SpecificationNode node = addNode(nodeQName);
        for(int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            QNameable parsedAttrName = SpecificationUtil.parseName(
                    _namespaces, _specification, _locator, attrName, nodeURI);
            QName attrQName = parsedAttrName.getQName();
            node.addAttribute(attrQName, attributes.getValue(i));
        }
        _current = node;
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        addCharactersNode();
        _current = _current.getParentNode();
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
    
    public void processingInstruction(String target, String data) {
        addCharactersNode();
		SpecificationNode node = addNode(QM_PROCESSING_INSTRUCTION);
		node.addAttribute(QM_TARGET, target);
		if(StringUtil.hasValue(data)) {
			node.addAttribute(QM_DATA, data);
		}
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

    public void fatalError(SAXParseException e) {
        error(e);
    }
    
    public void error(SAXParseException e) {
        if(LOG.isErrorEnabled()) {
            LOG.trace(e);
        }
        throw new RuntimeException(e);
    }

}

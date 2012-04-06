/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;
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

    protected static final QName QM_DATA =
        SpecificationUtil.createQName("data");
    protected static final QName QM_PUBLIC_ID =
        SpecificationUtil.createQName("publicID");
    protected static final QName QM_SYSTEM_ID =
        SpecificationUtil.createQName("systemID");
    protected static final QName QM_TARGET =
        SpecificationUtil.createQName("target");

    private Specification _specification;
    private SequenceIDGenerator _sequenceIDGenerator;
    private NodeTreeWalker _current;
    private Locator _locator;
    private Namespace _namespace;
    private StringBuffer _charactersBuffer;
    private int _charactersStartLineNumber;
    private boolean _outputMayaaWhitespace = false;
    private int _inEntity;
    private Map/*<NodeTreeWalker,Map<String(prefix),String(uri)>>*/
                _internalNamespacePrefixMap;
    private boolean _inCData;

    // TODO doctype宣言後の改行をテンプレート通りにしたあと削除
    private boolean _afterDocType;// workaround for doctype

    public SpecificationNodeHandler(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        _specification = specification;
        _current = _specification;      // by kato
        _sequenceIDGenerator = specification;
    }

    protected Specification getSpecification() {
        return _specification;
    }

    protected NodeTreeWalker getCurrentNode() {
        return _current;
    }

    protected void enterCData() {
        _inCData = true;
    }

    protected void leaveCData() {
        _inCData = false;
    }

    protected void setCurrentNode(NodeTreeWalker newCurrent) {
        _current = newCurrent;
    }

    public void setOutputMayaaWhitespace(boolean outputMayaaWhitespace) {
        _outputMayaaWhitespace = outputMayaaWhitespace;
    }

    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    protected void initNamespace() {
        _namespace = SpecificationUtil.createNamespace();
        URI defaultURI = BuilderUtil.getPrefixMapping(_specification.getSystemID()).getNamespaceURI();
        if (defaultURI == URI_XML) {
            defaultURI = URI_HTML;
        }
        getCurrentInternalNamespacePrefixMap().put("", defaultURI);
        getCurrentInternalNamespacePrefixMap().put("xml", URI_XML);
    }

    protected void pushNamespace(Namespace newNamespace) {
        _namespace = newNamespace;
    }

    protected void popNamespace() {
        _namespace = _namespace.getParentSpace();
        if (_namespace == null) {
            throw new IllegalStateException(getClass().getName());
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 128;

    protected void initCharactersBuffer() {
        _charactersBuffer = new StringBuffer(DEFAULT_BUFFER_SIZE);
        _charactersStartLineNumber = _locator.getLineNumber();
    }

    protected void appendCharactersBuffer(String str) {
        if (_charactersStartLineNumber == -1) {
            _charactersStartLineNumber = _locator.getLineNumber();
        }
        _charactersBuffer.append(str);
    }

    protected void appendCharactersBuffer(char str[], int offset, int len) {
        if (_charactersStartLineNumber == -1) {
            _charactersStartLineNumber = _locator.getLineNumber();
        }
        _charactersBuffer.append(str, offset, len);
    }

    public void startDocument() {
        _sequenceIDGenerator.resetSequenceID(1);
        initCharactersBuffer();
        _charactersStartLineNumber = -1;
        _current = _specification;
        _internalNamespacePrefixMap = new HashMap();
        initNamespace();
    }

    protected Map getCurrentInternalNamespacePrefixMap() {
        Map result = (Map)_internalNamespacePrefixMap.get(_current);
        if (result == null) {
            result = new HashMap();
            _internalNamespacePrefixMap.put(_current, result);
        }
        return result;
    }

    public void startPrefixMapping(String prefix, String uri) {
        getCurrentInternalNamespacePrefixMap().put(prefix,
                SpecificationUtil.createURI(uri));
    }

    public void endPrefixMapping(String prefix) {
        getCurrentInternalNamespacePrefixMap().remove(prefix);
    }

    protected SpecificationNode addNode(QName qName) {
        int lineNumber = _locator.getLineNumber();
        return addNode(qName, lineNumber);
    }

    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, false, sequenceID);
    }

    protected SpecificationNode addNode(QName qName, int lineNumber) {
        String systemID = StringUtil.removeFileProtocol(_locator.getSystemId());
        SpecificationNode child = createChildNode(
                qName, systemID, lineNumber, _sequenceIDGenerator.nextSequenceID());

        child.setParentSpace(SpecificationUtil.getFixedNamespace(_namespace));
        _current.addChildNode(child);
        return child;
    }

    protected boolean isRemoveWhitespace() {
        return _outputMayaaWhitespace == false
                    && _inCData == false;
    }

    protected void addCharactersNode() {
        if (_charactersBuffer.length() > 0) {
            String characters = _charactersBuffer.toString();
            if (isRemoveWhitespace()) {
                characters = removeIgnorableWhitespace(characters);
            }
            if (characters.length() > 0) {
                SpecificationNode node =
                    addNode(QM_CHARACTERS, _charactersStartLineNumber);
                node.addAttribute(QM_TEXT, characters);
            }
            initCharactersBuffer();
        }
    }

    private String removeIgnorableWhitespace(String characters) {
        StringBuffer buffer = new StringBuffer(characters.length());
        String[] line = characters.split("\n");
        for (int i = 0; i < line.length; i++) {
            if (line[i].trim().length() > 0) {
                String token = line[i].replaceAll("^[ \t]+", "");
                token = token.replaceAll("[ \t]+$", "");
                buffer.append(token.replaceAll("[ \t]+", " "));
                if (i < line.length - 1
                        && ((i + 1 < line.length - 1)
                                || (line[i + 1].trim().length() > 0))) {
                    buffer.append("\n");
                }
            }
        }
        return buffer.toString();
    }

    protected void saveToCycle(NodeTreeWalker originalNode) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.setOriginalNode(originalNode);
    }

    protected boolean checkAttribute(String qName, String value) {
        // workaround for XML parser(NekoHTML?)'s bug.
        if (StringUtil.isEmpty(qName)) {
            throw new IllegalArgumentException();
        }
        return ("xmlns".equals(qName) == false
                && qName.startsWith("xmlns:") == false);
    }

    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        // TODO doctype宣言後の改行をテンプレート通りにする
        // workaround NekoHTMLParserがdoctype宣言後に"\n"のみを含めてしまう
        // また、xercesそのままの場合は改行文字が来ない
        if (_afterDocType) {
            _afterDocType = false;// workaround for doctype
            int length = _charactersBuffer.length();
            if (length > 0) {
                int firstCharIndex;
                for (firstCharIndex = 0; firstCharIndex < length; firstCharIndex++) {
                    char currentChar = _charactersBuffer.charAt(firstCharIndex);
                    if (currentChar != ' ' && currentChar != '\t') {
                        break;
                    }
                }
                if (_charactersBuffer.charAt(firstCharIndex) == '\n') {
                    _charactersBuffer.insert(firstCharIndex, '\r');
                } else if (_charactersBuffer.charAt(firstCharIndex) != '\r') {
                    _charactersBuffer.insert(firstCharIndex, "\r\n");
                }
            } else {
                _charactersBuffer.insert(0, "\r\n");
            }
        }// workaround

        addCharactersNode();

        Namespace elementNS = SpecificationUtil.createNamespace();
        Iterator it = getCurrentInternalNamespacePrefixMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            elementNS.addPrefixMapping((String)entry.getKey(), (URI)entry.getValue());
        }
        elementNS.setParentSpace(SpecificationUtil.getFixedNamespace(_namespace));

        PrefixAwareName parsedName =
            BuilderUtil.parseName(elementNS, qName);
        QName nodeQName = parsedName.getQName();
        URI nodeURI = nodeQName.getNamespaceURI();
        elementNS.setDefaultNamespaceURI(nodeURI);
        elementNS = SpecificationUtil.getFixedNamespace(elementNS);

        SpecificationNode node = addNode(nodeQName);
        it = getCurrentInternalNamespacePrefixMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            node.addPrefixMapping((String)entry.getKey(), (URI)entry.getValue());
        }
        node.setDefaultNamespaceURI(nodeURI);

        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if (checkAttribute(attrName, attrValue)) {
                PrefixAwareName parsedAttrName =
                    BuilderUtil.parseName(elementNS, attrName);
                QName attrQName = parsedAttrName.getQName();
                node.addAttribute(attrQName, attrName, attrValue);
            }
        }
        NodeTreeWalker parent = _current; // by kato
        _current = node;
        _current.setParentNode(parent);
        saveToCycle(_current);
        pushNamespace(elementNS);
    }

    public void endElement(String namespaceURI,
            String localName, String qName) {
        popNamespace();
        addCharactersNode();
        _current = _current.getParentNode();
        saveToCycle(_current);
    }

    public void endDocument() {
        saveToCycle(_specification);
        _internalNamespacePrefixMap.clear();
        _internalNamespacePrefixMap = null;
        _current = null;
    }

    public void characters(char[] buffer, int start, int length) {
        if (_inEntity == 0) {
            appendCharactersBuffer(buffer, start, length);
        }
    }

    public void ignorableWhitespace(char[] buffer, int start, int length) {
        // no-op (white-spaces in element)
    }

    public void xmlDecl(String version, String encoding, String standalone) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, "xml");
        StringBuffer buffer = new StringBuffer();
        if (StringUtil.hasValue(version)) {
            buffer.append("version=\"").append(version).append("\" ");
        }
        if (StringUtil.hasValue(encoding)) {
            buffer.append("encoding=\"").append(
                    CharsetConverter.encodingToCharset(encoding)).append("\" ");
        }
        if (StringUtil.hasValue(standalone)) {
            buffer.append("standalone=\"").append(standalone).append("\" ");
        }
        if (buffer.length() > 0) {
            node.addAttribute(QM_DATA, buffer.toString().trim());
        }
    }

    public void processingInstruction(String target, String data) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, target);
        if (StringUtil.hasValue(data)) {
            node.addAttribute(QM_DATA, data);
        }
    }

    public void skippedEntity(String name) {
        // do nothing.
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return null;
    }

    protected void processEntity(String name) {
        appendCharactersBuffer(StringUtil.resolveEntity('&' + name + ';'));
    }

    public void startEntity(String name) {
        processEntity(name);
        ++_inEntity;
    }

    public void endEntity(String name) {
        --_inEntity;
    }

    public void comment(char[] buffer, int start, int length) {
        // do nothing.
    }

    public void notationDecl(String name, String publicId, String systemId) {
        // do nothing.
    }

    public void unparsedEntityDecl(
            String name, String publicId, String systemId, String notationName) {
        // do nothing.
    }

    public void startDTD(String name, String publicID, String systemID) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_DOCTYPE);
        node.addAttribute(QM_NAME, name);
        if (StringUtil.hasValue(publicID)) {
            node.addAttribute(QM_PUBLIC_ID, publicID);
        }
        if (StringUtil.hasValue(systemID)) {
            node.addAttribute(QM_SYSTEM_ID, systemID);
        }

        // TODO doctype宣言後の改行をテンプレート通りにしたあと削除
        _afterDocType = true;// workaround for doctype
    }

    public void endDTD() {
        // do nothing.
    }

    public void startCDATA() {
        enterCData();
    }

    public void endCDATA() {
        leaveCData();
    }

    private String exceptionMessage(SAXParseException e) {
        return
            "The problem occurred during Perse. " +
            _specification.getSystemID() +
            ((e.getMessage() != null)?" - " + e.getMessage(): "");
    }

    public void warning(SAXParseException e) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(exceptionMessage(e), e);
        }
    }

    public void fatalError(SAXParseException e) {
        if (LOG.isFatalEnabled()) {
            LOG.fatal(exceptionMessage(e), e);
        }
        throw new RuntimeException(exceptionMessage(e), e);
    }

    public void error(SAXParseException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(exceptionMessage(e), e);
        }
        throw new RuntimeException(exceptionMessage(e), e);
    }

}

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

import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.parser.AdditionalHandler;
import org.seasar.mayaa.impl.builder.parser.ParserEncodingChangedException;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.engine.specification.NamespaceImpl;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.URIImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class SpecificationNodeHandler
        implements ContentHandler,
        ErrorHandler, AdditionalHandler, CONST_IMPL {

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
    protected Locator _locator;
    private Stack<Namespace> _namespaceStack = new Stack<>();
    protected StringBuilder _charactersBuffer;
    private int _charactersStartLineNumber;
    private boolean _outputMayaaWhitespace = false;
    private boolean _inCData;

    private String _encoding;

    /**
     * {@link Specification} をファイルから読み込む際の最上位の名前空間設定を返す。
     * *.mayaaファイルの場合は {@link URI_MAYAA} をデフォルトとする。
     * @return 
     */
    abstract Namespace getTopLevelNamespace();

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

    private void initNamespace() {
        Namespace ns = getTopLevelNamespace();
        _namespaceStack.push(ns);
    }

    protected void pushNamespace(Namespace newNamespace) {
        _namespaceStack.push(newNamespace);
    }

    protected void popNamespace() {
        try {
            _namespaceStack.pop();
        } catch (EmptyStackException e) {
            throw new IllegalStateException(getClass().getName());
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 128;

    protected void initCharactersBuffer() {
        _charactersBuffer = new StringBuilder(DEFAULT_BUFFER_SIZE);
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
        _specification.clearChildNodes();
        _sequenceIDGenerator.resetSequenceID(1);
        initCharactersBuffer();
        _charactersStartLineNumber = -1;
        _current = _specification;
        initNamespace();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID, String prefix) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, false, sequenceID);
    }

    protected SpecificationNode addNode(QName qName, String prefix, int lineNumber) {
        if (lineNumber == -1) {
            lineNumber = _locator.getLineNumber();
        }
        String systemID = StringUtil.removeFileProtocol(_locator.getSystemId());
        SpecificationNode child = createChildNode(
                qName, systemID, lineNumber, _sequenceIDGenerator.nextSequenceID(), prefix);

        child.setParentSpace(_namespaceStack.peek());
        _current.addChildNode(child);
        return child;
    }

    protected SpecificationNode addNode(QName qName) {
        return addNode(qName, null, -1);
    }

    protected SpecificationNode addNode(String qName, Namespace namespace) {
        QName nodeQName;
        String prefix = null;

        URI namespaceURI = URIImpl.NULL_NS_URI;
        int colonIndex = qName.indexOf(':');
        if (colonIndex != -1) {
            prefix = qName.substring(0, colonIndex);
            String localName = qName.substring(colonIndex+1);
            PrefixMapping mapping = namespace.getMappingFromPrefix(prefix, true);
            if (mapping != null) {
                namespaceURI = mapping.getNamespaceURI();
            }
            nodeQName = QNameImpl.getInstance(namespaceURI, localName);
        } else {
            nodeQName = QNameImpl.getInstance(namespace.getDefaultNamespaceURI(), qName);
        }

        return addNode(nodeQName, prefix, -1);
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
                    addNode(QM_CHARACTERS, null, _charactersStartLineNumber);
                node.addAttribute(QM_TEXT, characters);
            }
            initCharactersBuffer();
        }
    }

    private String removeIgnorableWhitespace(String characters) {
        StringBuilder buffer = new StringBuilder(characters.length());
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

    protected boolean isXmlNamespaceDecl(String qName, String value) {
        return "xmlns".equals(qName) || qName.startsWith("xmlns:");
    }

    @Override
    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {

        // エレメントが始まるまでの文字をテキストノードバッファに追加
        addCharactersNode();

        Namespace parentNamespace = _namespaceStack.peek();
        Namespace elementNS = new NamespaceImpl();

        URI defaultURI = parentNamespace.getDefaultNamespaceURI();

        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if (isXmlNamespaceDecl(attrName, attrValue)) {
                int index = attrName.indexOf(':');
                if (index != -1) {
                    elementNS.addPrefixMapping(attrName.substring(index + 1), URIImpl.getInstance(attrValue));
                } else {
                    defaultURI = URIImpl.getInstance(attrValue);
                    elementNS.addPrefixMapping("", defaultURI);
                }
            }
        }
        elementNS.setParentSpace(parentNamespace);
        elementNS.setDefaultNamespaceURI(defaultURI);

        pushNamespace(elementNS);

        SpecificationNode node = addNode(qName, elementNS);

        // 現ノードの名前空間設定をnodeオブジェクトへ反映
        node.setDefaultNamespaceURI(elementNS.getDefaultNamespaceURI());
        node.setParentSpace(elementNS.getParentSpace());
        Iterator<PrefixMapping> itr = elementNS.iteratePrefixMapping(false);
        while (itr.hasNext()) {
            PrefixMapping mapping = itr.next();
            node.addPrefixMapping(mapping.getPrefix(), mapping.getNamespaceURI());
        }

        // 属性を格納
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getQName(i);
            String attrValue = attributes.getValue(i);
            if (!isXmlNamespaceDecl(attrName, attrValue)) {
                PrefixAwareName parsedAttrName = BuilderUtil.parseName(node, attrName);
                QName attrQName = parsedAttrName.getQName();
                node.addAttribute(attrQName, attrName, attrValue);
            }
        }
        NodeTreeWalker parent = _current; // by kato
        _current = node;
        _current.setParentNode(parent);
        saveToCycle(_current);
    }

    @Override
    public void endElement(String namespaceURI,
            String localName, String qName) {
        popNamespace();
        addCharactersNode();
        _current = _current.getParentNode();
        saveToCycle(_current);
    }

    @Override
    public void endDocument() {
        saveToCycle(_specification);
        _current = null;
    }

    @Override
    public void characters(char[] buffer, int start, int length) {
        appendCharactersBuffer(buffer, start, length);
    }

    @Override
    public void ignorableWhitespace(char[] buffer, int start, int length) {
        // no-op (white-spaces in element)
    }

    @Override
    public void xmlDecl(String version, String encoding, String standalone) {
        if (_encoding == null || _encoding.isEmpty()) {
            if (!encoding.equals(CONST_IMPL.TEMPLATE_DEFAULT_CHARSET)) {
                throw new ParserEncodingChangedException(encoding);
            }
        } else if (!encoding.equals(_encoding)) {
            throw new ParserEncodingChangedException(encoding);
        }

        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, "xml");
        StringBuilder buffer = new StringBuilder();
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

    @Override
    public void processingInstruction(String target, String data) {
        addCharactersNode();
        SpecificationNode node = addNode(QM_PI);
        node.addAttribute(QM_TARGET, target);
        if (StringUtil.hasValue(data)) {
            node.addAttribute(QM_DATA, data);
        }
    }

    @Override
    public void skippedEntity(String name) {
        // do nothing.
    }

    private String exceptionMessage(SAXParseException e) {
        return
            "The problem occurred during Perse. " +
            _specification.getSystemID() +
            ((e.getMessage() != null)?" - " + e.getMessage(): "");
    }

    @Override
    public void warning(SAXParseException e) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(exceptionMessage(e), e);
        }
    }

    @Override
    public void fatalError(SAXParseException e) {
        if (LOG.isFatalEnabled()) {
            LOG.fatal(exceptionMessage(e), e);
        }
        throw new RuntimeException(exceptionMessage(e), e);
    }

    @Override
    public void error(SAXParseException e) {
        if (LOG.isErrorEnabled()) {
            LOG.error(exceptionMessage(e), e);
        }
        throw new RuntimeException(exceptionMessage(e), e);
    }

    public String getSpecifiedEncoding() {
        return _encoding;
    }

    public void setSpecifiedEncoding(String encoding) {
        _encoding = encoding;
    }


}

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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.builder.parser.ParserEncodingChangedException;
import org.seasar.mayaa.impl.engine.CharsetConverter;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.NamespaceImpl;
import org.seasar.mayaa.impl.engine.specification.QNameImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.URIImpl;
import org.seasar.mayaa.impl.util.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TemplateNodeHandler extends SpecificationNodeHandler implements EntityResolver2, LexicalHandler {

    private static final Log LOG = LogFactory.getLog(TemplateNodeHandler.class);

    private SpecificationNode _dtdNode = null;
    private boolean _outputTemplateWhitespace = true;
    private boolean _isSSIIncludeReplacementEnabled = false;

    private static final QName QH_ATTR_HTTP_EQUIV = QNameImpl.getInstance(CONST_IMPL.URI_HTML, "http-equiv");
    private static final QName QH_ATTR_CONTENT = QNameImpl.getInstance(CONST_IMPL.URI_HTML, "content");
    private static final QName QX_ATTR_HTTP_EQUIV = QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "http-equiv");
    private static final QName QX_ATTR_CONTENT = QNameImpl.getInstance(CONST_IMPL.URI_XHTML, "content");
    private static final QName QH_ATTR_CHARSET = QNameImpl.getInstance(CONST_IMPL.URI_HTML, "charset");

    protected int _columnNumberBeforeFirstElement = -1;
    private int _inEntity;

    public TemplateNodeHandler(Template specification) {
        super(specification);
    }

    protected Template getTemplate() {
        return (Template) getSpecification();
    }

    public void setOutputTemplateWhitespace(boolean outputTemplateWhitespace) {
        _outputTemplateWhitespace = outputTemplateWhitespace;
    }

    public void setSSIIncludeReplacementEnabled(boolean isSSIIncludeReplacementEnabled) {
        _isSSIIncludeReplacementEnabled = isSSIIncludeReplacementEnabled;
    }

    /**
     * SSI includeの記述をm:insertへ置き換える処理が有効か。
     * @return 有効なら{@code true}、そうでなければ{@code false}を返す。
     */
    protected boolean isSSIIncludeReplacementEnabled() {
        return _isSSIIncludeReplacementEnabled;
    }

    /**
     * テンプレート由来のノードオブジェクトを生成する。
     */
    @Override
    protected SpecificationNode createChildNode(
            QName qName, String systemID, int lineNumber, int sequenceID, String prefix) {
        return SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, true, sequenceID, prefix);
    }

    protected boolean isRemoveWhitespace() {
        return _outputTemplateWhitespace == false;
    }

    /**
     * metaタグのhttp-equiv="content-type" の場合、contentのcharsetを変換して
     * ノードの属性にセットする。
     * それ以外の場合はなにもしない。
     *
     * @param node 対象となるSpecificationNodeインスタンス
     */
    final void convertContentType(final SpecificationNode node) {
        if (URI_HTML.equals(node.getDefaultNamespaceURI())) {
            convertContentType(node, QH_ATTR_HTTP_EQUIV, QH_ATTR_CONTENT);
        } else if (URI_XHTML.equals(node.getDefaultNamespaceURI())) {
            convertContentType(node, QX_ATTR_HTTP_EQUIV, QX_ATTR_CONTENT);
        }
    }
    protected void convertContentType(final SpecificationNode node, final QName attrHttpEquiv, final QName attrContent) {
        NodeAttribute na = node.getAttribute(attrHttpEquiv);
        if (na != null && "content-type".equalsIgnoreCase(na.getValue())) {
            na = node.getAttribute(attrContent);
            if (na != null) {
                String contentType = na.getValue();
                String encoding = CharsetConverter.extractEncodingExplict(contentType);
                String cunnrentEncoding = getSpecifiedEncoding();
                if (cunnrentEncoding == null || cunnrentEncoding.isEmpty()) {
                    if (encoding != null && !encoding.equals(CONST_IMPL.TEMPLATE_DEFAULT_CHARSET)) {
                        throw new ParserEncodingChangedException(encoding);
                    }
                } else if (encoding != null && !encoding.equals(cunnrentEncoding)) {
                    throw new ParserEncodingChangedException(encoding);
                }
                contentType = CharsetConverter.convertContentType(na.getValue());
                node.removeAttribute(attrContent);
                node.addAttribute(attrContent, contentType);
                return;
            }
        }
        // <meta charset="UTF-8">
        na = node.getAttribute(QH_ATTR_CHARSET);
        if (na != null) {
            String encoding = na.getValue();
            String cunnrentEncoding = getSpecifiedEncoding();
            if (cunnrentEncoding == null || cunnrentEncoding.isEmpty()) {
                if (encoding != null && !encoding.equals(CONST_IMPL.TEMPLATE_DEFAULT_CHARSET)) {
                    throw new ParserEncodingChangedException(encoding);
                }
            } else if (encoding != null && !encoding.equals(cunnrentEncoding)) {
                throw new ParserEncodingChangedException(encoding);
            }
            encoding = CharsetConverter.encodingToCharset(encoding);

            node.removeAttribute(QH_ATTR_CHARSET);
            node.addAttribute(QH_ATTR_CHARSET, encoding);
        }
    }
    @Override
    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {

        super.startElement(namespaceURI, localName, qName, attributes);
        if (qName.equalsIgnoreCase("meta")) {
            convertContentType((SpecificationNode) getCurrentNode());
        }
    }

    @Override
    public void ignorableWhitespace(char[] buffer, int start, int length) {
        appendCharactersBuffer(buffer, start, length);
    }

    @Override
    public void comment(char[] buffer, int start, int length) {
        addCharactersNode();
        String comment = new String(buffer, start, length);

        if (isSSIIncludeReplacementEnabled()) {
            String trimmed = comment.trim();
            if (trimmed.startsWith(INCLUDE_PREFIX)) {
                Matcher match = INCLUDE_PATTERN.matcher(trimmed);
                if (match.find()) {
                    LOG.debug("replace SSI to m:insert. " + trimmed);
                    includeToInsert(match.group(2));
                    return;
                }
            }
        }

        SpecificationNode node = addNode(QM_COMMENT);
        node.setDefaultNamespaceURI(node.getParentSpace().getDefaultNamespaceURI());
        node.addAttribute(QM_TEXT, comment);
    }

    @Override
    public void startCDATA() {
        addCharactersNode();
        SpecificationNode node = addNode(QM_CDATA);

        node.setParentNode(getCurrentNode());
        setCurrentNode(node);
        enterCData();
    }

    @Override
    public void endCDATA() {
        addCharactersNode();
        setCurrentNode(getCurrentNode().getParentNode());
        leaveCData();
    }

    @Override
    public void xmlDecl(String version, String encoding, String standalone) {
        super.xmlDecl(version, encoding, standalone);
        if ("xml".equalsIgnoreCase(getTemplate().getExtension())) {
            // XMLファイルはXercesパーサが使用されるがXML宣言の後の改行を返却してこないため強制的に付加する.
            //　改行がない時も付加してしまうが、互換性のため残す。
            appendCharactersBuffer("\n");
        }
    }

    /**
     * &lt;!--#include virtual="/common/include.html?foo=bar&amp;amp;bar=baz#fragment" --&gt;
     */
    private static final String INCLUDE_PREFIX = "#include";
    private static final Pattern INCLUDE_PATTERN =
        Pattern.compile("#include\\s+(virtual|file)\\s*=\\s*\"([^\"]+)\"\\s*");
    private static final Pattern AMPERSAND_PATTERN = Pattern.compile("(&amp;|&)");
    protected static final String AUTO_INSERT_NAMESPACE = "autoinsertnamespace";

    /**
     * SSI includeの記述を m:insert に置き換える。virtual, fileの両方に対応。
     * ただし、本来fileでは上位階層を指定できないが、ここでは制限を意識しない。
     * <p>
     * &lt;!--#include virtual="/common/include.html?foo=bar&amp;amp;bar=baz#fragment" --&gt;
     * </p>
     * @param virtualValue virtual属性の値
     * @throws NullPointerException virtualValueがnullの場合に発生する。
     */
    protected void includeToInsert(String virtualValue) {
        if (virtualValue == null) {
            throw new IllegalArgumentException("virtualValue must not null.");
        }
        String[] parsed = StringUtil.parseURIQuery(virtualValue, EngineUtil.getEngineSetting(CONST_IMPL.SUFFIX_SEPARATOR, "$"));

        includeToInsert(parsed[0], parsed[1], parsed[2]);
    }

    protected void includeToInsert(String componentPath, String query, String componentName) {
        URI namespace = URI_MAYAA;
        NodeTreeWalker current = getCurrentNode();
        if (current instanceof SpecificationNode) {
            namespace = ((SpecificationNode) current).getDefaultNamespaceURI();
        }
        SpecificationNode newNode = addNode(QNameImpl.getInstance(namespace, "span"));
        newNode.setDefaultNamespaceURI(newNode.getParentSpace().getDefaultNamespaceURI());

        String mayaaPrefix = "mmmmmmmmmmmm";// 重複しないようにまず使われないものを。
        newNode.addPrefixMapping(mayaaPrefix, URI_MAYAA);
        newNode.addAttribute(QM_INJECT, mayaaPrefix + ":insert");
        newNode.addAttribute(QNameImpl.getInstance("path"), componentPath);
        if (StringUtil.hasValue(componentName)) {
            newNode.addAttribute(QNameImpl.getInstance("name"), componentName);
        }
        if (StringUtil.hasValue(query)) {
            URI parameterURI = URIImpl.getInstance(AUTO_INSERT_NAMESPACE);// 重複しないようにまず使われないものを。
            String[] parameters = AMPERSAND_PATTERN.split(query);
            for (int i = 0; i < parameters.length; i++) {
                String name;
                String value;
                int eq = parameters[i].indexOf('=');
                if (eq > 0) {
                    name = parameters[i].substring(0, eq).trim();
                    value = parameters[i].substring(eq + 1).trim();
                } else {
                    name = parameters[i].trim();
                    value = "";
                }
                newNode.addAttribute(QNameImpl.getInstance(parameterURI, name), value);
            }
        }
    }

    //- implementation of DTD Catalog.
    // Java9 より標準の javax.xml.catalog.CatalogReader が提供されるので最低バージョンが上がるタイミングで標準半に置き換えたい。
    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return this.resolveEntity(null, publicId, null, systemId);
    }

    @Override
    /**
     * xhtmlのパースの際にリモートのDTD定義を取得しに行こうとするためJAR内に格納したものを返す。
     */
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {

        String path = null;
        switch (systemId) {
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd":
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd":
            case "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd":
                path = "/dtd/" + systemId.substring("http://www.w3.org/".length());
                break;
            case "xhtml-lat1.ent":
            case "xhtml-symbol.ent":
            case "xhtml-special.ent":
                path = "/dtd/" + systemId;
                break;
            default:
                break;
        }
        if (path != null) {
            InputStream is = getClass().getResourceAsStream(path);
            return new InputSource(is);
        }
        return null;
    }


    @Override
    Namespace getTopLevelNamespace() {
        Specification spec = getSpecification();
        PrefixMapping defaultMapping = BuilderUtil.getPrefixMapping(spec.getSystemID());
        URI defaultURI = defaultMapping.getNamespaceURI();

        Namespace _topLevelNamespace = new NamespaceImpl();
        _topLevelNamespace.setDefaultNamespaceURI(defaultURI);
        return _topLevelNamespace;
    }

    @Override
    public void startDTD(String name, String publicId, String systemId) {
        if (_dtdNode == null) {
            addCharactersNode();
            _dtdNode = addNode(QM_DOCTYPE);
        }
        _dtdNode.removeAttribute(QM_NAME);
        _dtdNode.addAttribute(QM_NAME, name);

        _dtdNode.removeAttribute(QM_PUBLIC_ID);
        if (StringUtil.hasValue(publicId)) {
            _dtdNode.addAttribute(QM_PUBLIC_ID, publicId);
        }
        _dtdNode.removeAttribute(QM_SYSTEM_ID);
        if (StringUtil.hasValue(systemId)) {
            _dtdNode.addAttribute(QM_SYSTEM_ID, systemId);
        }
    }

    @Override
    public void endDTD() {
    }

    @Override
    public void characters(char[] buffer, int start, int length) {
        if (_inEntity == 0) {
            appendCharactersBuffer(buffer, start, length);
        }
    }

    protected void processEntity(String name) {
        appendCharactersBuffer('&' + name + ';');
    }

    @Override
    public void startEntity(String name) {
        processEntity(name);
        ++_inEntity;
    }

    @Override
    public void endEntity(String name) {
        --_inEntity;
    }

 }

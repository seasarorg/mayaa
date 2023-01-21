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

import org.seasar.mayaa.engine.Template;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.knowledge.HTMLKnowledge;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class NekoHtmlNodeHandler extends TemplateNodeHandler {

    private boolean _justAfterXmlDecl = false;
    private boolean inDTD = false;
    private int _inEntity;
    private SpecificationNode _lastVoidElement;

    public NekoHtmlNodeHandler(Template specification) {
        super(specification);
    }

    @Override
    public void characters(char[] buffer, int start, int length) {
        if (_justAfterXmlDecl) {
            _justAfterXmlDecl = false;
            if (buffer[start] == '\r') {
                start++;
                length--;
            }
            if (length > 0 && buffer[start] == '\n') {
                start++;
                length--;
            }
            if (length <= 0) {
                return;
            }
        }
        if (_inEntity == 0) {
            super.characters(buffer, start, length);
        }
    }
    @Override
    public void xmlDecl(String version, String encoding, String standalone) {
        super.xmlDecl(version, encoding, standalone);
        _justAfterXmlDecl = true;
    }

    @Override
    public void startElement(String namespaceURI,
            String localName, String qName, Attributes attributes) {
        if (_justAfterXmlDecl) {
            _justAfterXmlDecl = false;
        }
        
        // エレメントが始まればDTD処理のフェーズは終了
        inDTD = false;

        super.startElement(namespaceURI, localName, qName, attributes);
        _lastVoidElement = null;
        NodeTreeWalker node = getCurrentNode();
        if (node instanceof SpecificationNode) {
            SpecificationNode sn = (SpecificationNode) node;
            if (HTMLKnowledge.isVoidElement(sn.getQName())) {
                super.endElement(namespaceURI, localName, qName);
                _lastVoidElement = sn;
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        if (_lastVoidElement instanceof SpecificationNode) {
            PrefixAwareName name = BuilderUtil.parseName(_lastVoidElement, qName);
            QName qn = _lastVoidElement.getQName();
            _lastVoidElement = null;
            if (name.getQName().equals(qn)) {
                return;
            }
        }
        super.endElement(namespaceURI, localName, qName);
    }

    @Override
    public void comment(char[] buffer, int start, int length) {
        // パーサがDTD定義をコメントに埋め込んでくるためDTD処理中であれば除外する。
        if (inDTD) {
            return;
        }
        super.comment(buffer, start, length);
    }
    //- LexicalHander
    @Override
    public void startEntity(String name) {
        if (inDTD) {
            return;
        }
        String entityRef = "&" + name + ";";
        appendCharactersBuffer(entityRef);
        ++_inEntity;
    }

    @Override
    public void endEntity(String name) {
        if (inDTD) {
            return;
        }
        --_inEntity;
    }

    @Override
    public void startDTD(String name, String publicID, String systemID) {
        inDTD = true;
        super.startDTD(name, publicID, systemID);
    }

    @Override
    public void endDTD() {
        inDTD = false;
    }
}

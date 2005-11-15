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
package org.seasar.mayaa.impl.engine.specification;

import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.script.ScriptUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationUtil implements CONST_IMPL {

    private SpecificationUtil() {
        // no instantiation.
    }

    public static String getAttributeValue(
            SpecificationNode node, QName qName) {
        NodeAttribute nameAttr = node.getAttribute(qName);
        if(nameAttr != null) {
            return nameAttr.getValue();
        }
        return null;
    }

    public static Specification findSpecification(NodeTreeWalker current) {
        while(current instanceof Specification == false) {
            current = current.getParentNode();
            if(current == null) {
                return null;
            }
        }
        return (Specification)current;
    }

    public static Specification findSpecification() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        NodeTreeWalker current = cycle.getOriginalNode();
        return findSpecification(current);
    }

    public static SpecificationNode getMayaaNode(NodeTreeWalker current) {
        Specification specification = findSpecification(current);
        for(Iterator it = specification.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode node = (SpecificationNode)it.next();
            if(node.getQName().equals(QM_MAYA)) {
                return node;
            }
        }
        return null;
    }

    public static String getMayaaAttributeValue(
            NodeTreeWalker current, QName qName) {
        SpecificationNode mayaa = getMayaaNode(current);
        if(mayaa != null) {
            String value = getAttributeValue(mayaa, qName);
            if(value != null) {
                return value;
            }
        }
        return null;
    }

    public static String getNodeBodyText(SpecificationNode node) {
        StringBuffer buffer = new StringBuffer();
        for(Iterator it = node.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode child = (SpecificationNode)it.next();
            QName qName = child.getQName();
            if(QM_CDATA.equals(qName)) {
                buffer.append(getNodeBodyText(child));
            } else if(QM_CHARACTERS.equals(qName)) {
                buffer.append(SpecificationUtil.getAttributeValue(
                        child, QM_TEXT));
            } else {
                String name = child.getPrefix() + ":" + qName.getLocalName();
                throw new IllegalChildNodeException(name);
            }
        }
        return buffer.toString();
    }

    public static void initScope() {
        ProviderUtil.getScriptEnvironment().initScope();
    }

    public static void startScope(Map variables) {
        ProviderUtil.getScriptEnvironment().startScope(variables);
    }

    public static void endScope() {
        ProviderUtil.getScriptEnvironment().endScope();
    }

    public static void execEventScript(String text) {
        if(StringUtil.hasValue(text)) {
            CompiledScript script =
                ScriptUtil.compile(text, Void.class);
            script.execute(null);
        }
    }

    public static void execEvent(Specification spec, QName eventName) {
        if(eventName == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode mayaa = getMayaaNode(spec);
        if(mayaa != null) {
            for(Iterator it = mayaa.iterateChildNode(); it.hasNext(); ) {
                SpecificationNode child = (SpecificationNode)it.next();
                if(eventName.equals(child.getQName())) {
                    String bodyText = getNodeBodyText(child);
                    bodyText = ScriptUtil.getBlockSignedText(bodyText);
                    execEventScript(bodyText);
                }
            }
            NodeAttribute attr = mayaa.getAttribute(eventName);
            if(attr != null) {
                String attrText = attr.getValue();
                execEventScript(attrText);
            }
        }
    }

    // factory methods ----------------------------------------------

    public static Namespace createNamespace() {
        return new NamespaceImpl();
    }

    public static QName createQName(String localName) {
        return createQName(URI_MAYA, localName);
    }

    public static QName createQName(
            String namespaceURI, String localName) {
        return new QNameImpl(namespaceURI, localName);
    }

    public static PrefixAwareName createPrefixAwareName(QName qName) {
        return new PrefixAwareNameImpl(qName);
    }

    public static SpecificationNode createSpecificationNode(
            QName qName, String systemID, int lineNumber,
            boolean onTemplate, int sequenceID) {
        SpecificationNodeImpl node = new SpecificationNodeImpl(qName);
        node.setSequenceID(sequenceID);
        node.setSystemID(systemID);
        node.setLineNumber(lineNumber);
        node.setOnTemplate(onTemplate);
        return node;
    }

}

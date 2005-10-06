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
package org.seasar.maya.impl.engine.specification;

import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.script.CompiledScript;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeTreeWalker;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.cycle.script.ScriptUtil;
import org.seasar.maya.impl.util.StringUtil;

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

    public static SpecificationNode getMayaNode(NodeTreeWalker current) {
        Specification specification = findSpecification(current);
        for(Iterator it = specification.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode node = (SpecificationNode)it.next();
            if(node.getQName().equals(QM_MAYA)) {
                return node;
            }
        }
        return null;
    }

    public static String getMayaAttributeValue(
            NodeTreeWalker current, QName qName) {
        SpecificationNode maya = getMayaNode(current);
        if(maya != null) {
            String value = getAttributeValue(maya, qName);
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
        ScriptUtil.getScriptEnvironment().initScope();
    }

    public static void startScope(Map variables) {
        ScriptUtil.getScriptEnvironment().startScope(variables);
    }

    public static void endScope() {
        ScriptUtil.getScriptEnvironment().endScope();
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
        SpecificationNode maya = getMayaNode(spec);
        if(maya != null) {
            for(Iterator it = maya.iterateChildNode(); it.hasNext(); ) {
                SpecificationNode child = (SpecificationNode)it.next();
                if(eventName.equals(child.getQName())) {
                    String bodyText = getNodeBodyText(child);
                    bodyText = ScriptUtil.getBlockSignedText(bodyText);
                    execEventScript(bodyText);
                }
            }
            NodeAttribute attr = maya.getAttribute(eventName);
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

    public static QNameable createQNameable(QName qName) {
        return new QNameableImpl(qName);
    }

    public static SpecificationNode createSpecificationNode(
            QName qName, String systemID, int lineNumber) {
        return new SpecificationNodeImpl(qName, systemID, lineNumber);
    }

}

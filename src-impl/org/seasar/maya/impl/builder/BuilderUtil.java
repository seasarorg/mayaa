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
package org.seasar.maya.impl.builder;

import java.util.Iterator;

import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.PrefixMapping;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.PrefixAwareName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BuilderUtil implements CONST_IMPL {

    private BuilderUtil() {
        // no instantiation.
    }

    public static SpecificationNode createInjectedNode(QName qName, 
            String uri, SpecificationNode original, boolean maya) {
        if(qName == null || original == null) {
            throw new IllegalArgumentException();
        }
        String systemID = original.getSystemID();
        int lineNumber = original.getLineNumber();
        boolean onTemplate = original.isOnTemplate();
        int sequenceID = original.getSequenceID();
        SpecificationNode node = SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, onTemplate, sequenceID);
        if(StringUtil.hasValue(uri)) {
            for(Iterator it = original.iterateAttribute(); it.hasNext(); ) {
                NodeAttribute attr = (NodeAttribute)it.next();
                String attrURI = attr.getQName().getNamespaceURI();
                if(uri.equals(attrURI) || (maya && URI_MAYA.equals(attrURI))) {
                    node.addAttribute(attr.getQName(), attr.getValue());
                }
            }
        }
        node.setParentSpace(original.getParentSpace());
        return node;
    }

    public static PrefixAwareName parseName(
            Namespace namespace, String qName) {
        String[] parsed = qName.split(":");
        String prefix = null;
        String localName = null;
        String namespaceURI = null;
        if(parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            PrefixMapping mapping =
                namespace.getMappingFromPrefix(prefix, true);
            if(mapping == null) {
                throw new PrefixMappingNotFoundException(prefix);
            }
            namespaceURI = mapping.getNamespaceURI();
        } else if(parsed.length == 1) {
            localName = parsed[0];
            PrefixMapping mapping =
                namespace.getMappingFromPrefix("", true);
            if(mapping != null) {
                namespaceURI = mapping.getNamespaceURI();
            } else {
                throw new PrefixMappingNotFoundException("");
            }
        } else {
            throw new IllegalNameException(qName);
        }
        PrefixAwareName ret = SpecificationUtil.createPrefixAwareName(
                SpecificationUtil.createQName(namespaceURI, localName));
        ret.setParentSpace(namespace);
        return ret;
    }

    
}

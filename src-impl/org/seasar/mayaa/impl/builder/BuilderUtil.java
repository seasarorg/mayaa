/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.Iterator;

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BuilderUtil implements CONST_IMPL {

    private BuilderUtil() {
        // no instantiation.
    }

    public static SpecificationNode createInjectedNode(QName qName,
            String uri, SpecificationNode original, boolean mayaa) {
        if (qName == null || original == null) {
            throw new IllegalArgumentException();
        }
        String systemID = original.getSystemID();
        int lineNumber = original.getLineNumber();
        boolean onTemplate = original.isOnTemplate();
        int sequenceID = original.getSequenceID();
        SpecificationNode node = SpecificationUtil.createSpecificationNode(
                qName, systemID, lineNumber, onTemplate, sequenceID);
        if (StringUtil.hasValue(uri)) {
            PathAdjuster adjuster = ProviderUtil.getPathAdjuster();

            boolean needAdjust = adjuster.isTargetNode(original.getQName());
            String basePath = null;
            if (needAdjust) {
                String contextPath = CycleUtil.getRequestScope().getContextPath();
                String sourcePath = EngineUtil.getSourcePath(original);
                basePath = contextPath + sourcePath;
            }

            for (Iterator it = original.iterateAttribute(); it.hasNext();) {
                NodeAttribute attr = (NodeAttribute) it.next();
                String attrURI = attr.getQName().getNamespaceURI();
                if (uri.equals(attrURI)
                        || (mayaa && URI_MAYAA.equals(attrURI))
                        || uri.equals(URI_XHTML) && URI_XML.equals(attrURI)) {
                    String attrValue = attr.getValue();
                    if (needAdjust
                            && adjuster.isTargetAttribute(
                                    original.getQName(), attr.getQName())) {
                        attrValue =
                            adjuster.adjustRelativePath(basePath, attrValue);
                    }
                    node.addAttribute(attr.getQName(), attrValue);
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
        if (parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            PrefixMapping mapping =
                namespace.getMappingFromPrefix(prefix, true);
            if (mapping == null) {
                throw new PrefixMappingNotFoundException(prefix);
            }
            namespaceURI = mapping.getNamespaceURI();
        } else if (parsed.length == 1) {
            localName = parsed[0];
            PrefixMapping mapping =
                namespace.getMappingFromPrefix("", true);
            if (mapping != null) {
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

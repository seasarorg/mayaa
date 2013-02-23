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

import java.util.Iterator;

import org.seasar.mayaa.builder.PathAdjuster;
import org.seasar.mayaa.builder.SequenceIDGenerator;
import org.seasar.mayaa.builder.library.LibraryManager;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.engine.processor.TemplateProcessor;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.engine.specification.PrefixMapping;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.processor.TemplateProcessorSupport;
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
            URI uri, SpecificationNode original, boolean mayaa) {
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
                String attrValue = attr.getValue();
                if (needAdjust
                        && adjuster.isTargetAttribute(
                                original.getQName(), attr.getQName())) {
                    attrValue =
                        adjuster.adjustRelativePath(basePath, attrValue);
                }
                String originalName = null;
                if (StringUtil.isEmpty(attr.getPrefix()) == false
                        && attr.getQName().getLocalName().indexOf(':') < 0) {
                    originalName =
                        attr.getPrefix() + ":"
                        + attr.getQName().getLocalName();
                }
                node.addAttribute(attr.getQName(), originalName, attrValue);
            }

            for (Iterator it = original.iteratePrefixMapping(false); it.hasNext();) {
                PrefixMapping prefixMapping = (PrefixMapping) it.next();
                node.addPrefixMapping(
                        prefixMapping.getPrefix(), prefixMapping.getNamespaceURI());
            }
        }
        //node.setParentSpace(SpecificationUtil.getFixedNamespace(original.getParentSpace()));
        node.setParentSpace(original.getParentSpace());
        node.setDefaultNamespaceURI(original.getDefaultNamespaceURI());
        return node;
    }

    /**
     * 指定したsystemIDからmimeTypeを見て、HTMLならHTML用のデフォルトPrefixMappingを、
     * それ以外ならXMLのデフォルトPrefixMappingを返します。
     *
     * @param systemID mime-typeを調べるsystemID
     * @return 適切なデフォルトPrefixMapping
     */
    static PrefixMapping getPrefixMapping(String systemID) {
        if (systemID != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            ApplicationScope application = cycle.getApplicationScope();
            String mimeType = application.getMimeType(systemID);
            if (mimeType != null) {
                if (mimeType.indexOf("xhtml") != -1) {
                    return SpecificationUtil.XHTML_DEFAULT_PREFIX_MAPPING;
                } else if (mimeType.indexOf("html") != -1) {
                    return SpecificationUtil.HTML_DEFAULT_PREFIX_MAPPING;
                }
            }
        }
        return SpecificationUtil.XML_DEFAULT_PREFIX_MAPPING;
    }

    /**
     * 現在ServiceCycleからmimeTypeを見て、HTMLならHTML用のデフォルトPrefixMappingを、
     * それ以外ならXMLのデフォルトPrefixMappingを返します。
     *
     * @return 適切なデフォルトPrefixMapping
     */
    static PrefixMapping getDefaultPrefixMapping() {
        NodeTreeWalker original = CycleUtil.getServiceCycle().getOriginalNode();
        return getPrefixMapping(original == null ? null : original.getSystemID());
    }

    public static PrefixAwareName parseName(
            Namespace namespace, String qName) {
        String[] parsed = qName.split(":");
        String prefix = null;
        String localName = null;
        URI namespaceURI = null;
        PrefixMapping mapping = null;
        if (parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            mapping = namespace.getMappingFromPrefix(prefix, true);
            if (mapping == null) {
                if ("xml".equals(prefix)) {
                    mapping = SpecificationUtil.XML_DEFAULT_PREFIX_MAPPING;
                } else {
                    // HTA:Applicationのような無作法なものもあるので対応できるようにする。
                    mapping = getDefaultPrefixMapping();
                    localName = qName;  // プレフィクスも含めて要素名扱いとする。
                    prefix = "";
                }
            }
            namespaceURI = mapping.getNamespaceURI();
        } else if (parsed.length == 1) {
            localName = parsed[0];
            namespaceURI = namespace.getDefaultNamespaceURI();

            if (namespaceURI == null) {
                mapping = namespace.getMappingFromPrefix("", true);
                if (mapping == null) {
                    mapping = getDefaultPrefixMapping();
                }
                namespaceURI = mapping.getNamespaceURI();
            }
        } else {
            throw new IllegalNameException(qName);
        }
        if (mapping != null) {
            prefix = mapping.getPrefix();
        } else {
            prefix = "";
        }
        PrefixAwareName ret = SpecificationUtil.createPrefixAwareName(
                SpecificationUtil.createQName(namespaceURI, localName),
                prefix);
        return ret;
    }

    static SpecificationNode nodeCopyOf(SpecificationNode node, QName newQName,
            SequenceIDGenerator sequenceIDGenerator) {
        if (newQName == null) {
            newQName = node.getQName();
        }
        return SpecificationUtil.createSpecificationNode(newQName,
                node.getSystemID(), node.getLineNumber(),
                true, sequenceIDGenerator.nextSequenceID());
    }

    public static void characterProcessorCopy(TemplateProcessor from,
            TemplateProcessorSupport to, SequenceIDGenerator idGenerator) {
        to.setOriginalNode(from.getOriginalNode());
        to.setInjectedNode(from.getInjectedNode());
        to.setEvalBodyInclude(false);
        LibraryManager libraryManager = ProviderUtil.getLibraryManager();
        to.setProcessorDefinition(
                libraryManager.getProcessorDefinition(CONST_IMPL.QM_CHARACTERS));
    }

}

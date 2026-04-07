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
package org.seasar.mayaa.impl.builder.injection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.NonSerializableParameterAwareImpl;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EqualsIDInjectionResolver extends NonSerializableParameterAwareImpl
        implements InjectionResolver {

    private static final CopyToFilter _idFilter = new CheckIDCopyToFilter();
    private List<QName> _additionalIds = new ArrayList<>();
    private boolean _reportResolvedID = true;
    private boolean _reportDuplicatedID = true;

    public EqualsIDInjectionResolver() {
        _additionalIds.add(CONST_IMPL.QM_ID);
    }

    protected CopyToFilter getCopyToFilter() {
        return _idFilter;
    }

    protected boolean isReportResolvedID() {
        return _reportResolvedID;
    }

    protected boolean isReportDuplicatedID() {
        return _reportDuplicatedID;
    }

    protected NodeAttribute getAttribute(SpecificationNode node) {
        for (Iterator<QName> it = _additionalIds.iterator(); it.hasNext();) {
            NodeAttribute attr = node.getAttribute(it.next());
            if (attr != null) {
                return attr;
            }
        }
        return null;
    }

    protected String getID(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        NodeAttribute attr = getAttribute(node);
        if (attr != null) {
            return attr.getValue();
        }
        return null;
    }

    protected void getEqualsIDNodes(
            SpecificationNode node, String id, List<SpecificationNode> specificationNodes) {
        if (node == null || StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException();
        }
        for (Iterator<NodeTreeWalker> it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();

            if (id.equals(SpecificationUtil.getAttributeValue(child, CONST_IMPL.QM_ID))) {
                if (CONST_IMPL.QM_MAYAA.equals(node.getQName())) {
                    specificationNodes.add(child);
                }
                // m:mayaa直下でない要素のid属性はinjectionに使われない（仕様）。
                // 警告は .mayaa ファイルのパース時に warnInvalidIdNodes() で一度だけ出す。
            }
            getEqualsIDNodes(child, id, specificationNodes);
        }
    }

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if (original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        String id = getID(original);
        if (StringUtil.hasValue(id)) {
            Specification spec = SpecificationUtil.findSpecification(original);
            SpecificationNode injected = null;
            while (spec != null) {
                SpecificationNode mayaa = SpecificationUtil.getMayaaNode(spec);
                if (mayaa != null) {
                    List<SpecificationNode> injectNodes = new ArrayList<>();
                    getEqualsIDNodes(mayaa, id, injectNodes);
                    if (injectNodes.size() > 0) {
                        injected = injectNodes.get(0);
                        if (isReportDuplicatedID() && injectNodes.size() > 1) {
                            logWarnning(id, original, 2);
                        }
                        break;
                    }
                }
                spec = EngineUtil.getParentSpecification(spec);
            }
            if (injected != null) {
                if (CONST_IMPL.QM_IGNORE.equals(injected.getQName())) {
                    return chain.getNode(original);
                }
                return injected.copyTo(getCopyToFilter());
            }
            if (isReportResolvedID()) {
                logWarnning(id, original, 1);
            }
        }
        return chain.getNode(original);
    }

    /**
     * .mayaaファイルパース後に呼び出す。m:mayaa直下でないノードにid属性がある場合、
     * 警告を一度だけ記録する。{@link SpecificationBuilderImpl#afterBuild} から呼ばれる。
     *
     * @param specification パース済みの .mayaa Specification
     */
    public static void warnInvalidIdNodes(Specification specification) {
        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(specification);
        if (mayaaNode == null) {
            return;
        }
        warnInvalidIdNodesRecursive(mayaaNode);
    }

    private static void warnInvalidIdNodesRecursive(SpecificationNode node) {
        for (Iterator<NodeTreeWalker> it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();
            String id = SpecificationUtil.getAttributeValue(child, CONST_IMPL.QM_ID);
            if (StringUtil.hasValue(id) && !CONST_IMPL.QM_MAYAA.equals(node.getQName())) {
                // m:mayaa直下でない要素のid属性はinjectionに使われない（仕様）
                // ユーザーに意図しない無効化を気づかせるために警告する
                String msg = StringUtil.getMessage(
                        EqualsIDInjectionResolver.class, 3,
                        id, child.getSystemID(), Integer.toString(child.getLineNumber()));
                DiagnosticEventBuffer.recordWarn(
                        DiagnosticEventBuffer.Phase.BUILD,
                        "parseInjectionID",
                        child.getSystemID(),
                        msg, null, id, child);
            }
            warnInvalidIdNodesRecursive(child);
        }
    }

    protected void logWarnning(String id, SpecificationNode node, int number) {
        String msg = StringUtil.getMessage(
                EqualsIDInjectionResolver.class, number,
                id, node.getSystemID(), Integer.toString(node.getLineNumber()));
        DiagnosticEventBuffer.recordWarn(
                DiagnosticEventBuffer.Phase.BUILD,
                "resolveInjectionID",
                node.getSystemID(),
                msg,
                null,
                id,
                node);
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("reportUnresolvedID".equals(name)) {
            _reportResolvedID = ObjectUtil.booleanValue(value, true);
        }
        if ("reportDuplicatedID".equals(name)) {
            _reportDuplicatedID = ObjectUtil.booleanValue(value, true);
        }
        if ("addAttribute".equals(name)) {
            _additionalIds.add(SpecificationUtil.parseQName(value));
        }
        super.setParameter(name, value);
    }

    // support class ------------------------------------------------

    protected static class CheckIDCopyToFilter implements CopyToFilter {

        public boolean accept(NodeObject test) {
            if (test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute) test;
                return attr.getQName().equals(CONST_IMPL.QM_ID) == false;
            }
            return true;
        }

    }

}

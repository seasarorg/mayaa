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

import org.seasar.mayaa.engine.specification.Specification;

import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.impl.management.DiagnosticEventBuffer;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.NonSerializableParameterAwareImpl;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.xpath.XPathUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * m:xpathに記述されたxpath式にマッチするノードをインジェクション対象とします。
 *
 * JIRA[MAYAA-28] position関数が正常動作しないバグあり。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class XPathMatchesInjectionResolver extends NonSerializableParameterAwareImpl
        implements InjectionResolver {

    protected static final QName QM_XPATH =
        SpecificationUtil.createQName("xpath");

    private static final CopyToFilter _xpathFilter = new CheckXPathCopyToFilter();

    protected CopyToFilter getCopyToFilter() {
        return _xpathFilter;
    }

    /**
     * Mayaaファイル内のm:xpathを持つノードを集め、リストに入れます。
     *
     * @param node Mayaaのノードツリー
     * @param specificationNodes m:xpathを持つノードのリスト
     */
    protected void getXPathNodes(
            SpecificationNode node, List<SpecificationNode> specificationNodes) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        for (Iterator<NodeTreeWalker> it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();
            String xpath = SpecificationUtil.getAttributeValue(child, QM_XPATH);
            if (StringUtil.hasValue(xpath)) {
                if (CONST_IMPL.QM_MAYAA.equals(node.getQName())) {
                    specificationNodes.add(child);
                }
                // m:mayaa直下でない要素は injection に使われない（仕様）。
                // 警告は .mayaa ファイルのパース時に SpecificationBuilderImpl.afterBuild() で一度だけ出力する。
            }
            getXPathNodes(child, specificationNodes);
        }
    }

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if (original == null || chain == null) {
            throw new IllegalArgumentException();
        }

        // TODO テンプレートのprefix定義、Mayaaファイルのを反映させる
        Namespace namespace = SpecificationUtil.createNamespace();
        namespace.addPrefixMapping("m", CONST_IMPL.URI_MAYAA);

        // mayaaファイル内のm:xpathを持つすべてのノードを対象とする
        Specification spec = SpecificationUtil.findSpecification(original);
        List<SpecificationNode> injectNodes = new ArrayList<>();
        while (spec != null) {
            SpecificationNode mayaa = SpecificationUtil.getMayaaNode(spec);
            if (mayaa != null) {
                getXPathNodes(mayaa, injectNodes);
            }
            spec = EngineUtil.getParentSpecification(spec);
        }

        for (Iterator<SpecificationNode> it = injectNodes.iterator(); it.hasNext();) {
            SpecificationNode injected = it.next();
            String mayaaPath = SpecificationUtil.getAttributeValue(
                    injected, QM_XPATH);
            // injectedをnamespaceとして渡すとfunctionのデフォルトnamesapceが
            // URI_MAYAAになってしまうため、デフォルトnamespaceのないものを渡す
            if (XPathUtil.matches(original, mayaaPath, namespace)) {
                return injected.copyTo(getCopyToFilter());
            }
        }
        return chain.getNode(original);
    }

    /**
     * .mayaaファイルパース後に呼び出す。m:mayaa直下でないノードにxpath属性がある場合、
     * 警告を一度だけ記録する。{@link SpecificationBuilderImpl#afterBuild} から呼ばれる。
     *
     * @param specification パース済みの .mayaa Specification
     */
    public static void warnInvalidXPathNodes(Specification specification) {
        SpecificationNode mayaaNode = SpecificationUtil.getMayaaNode(specification);
        if (mayaaNode == null) {
            return;
        }
        warnInvalidXPathNodesRecursive(mayaaNode);
    }

    private static void warnInvalidXPathNodesRecursive(SpecificationNode node) {
        for (Iterator<NodeTreeWalker> it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();
            String xpath = SpecificationUtil.getAttributeValue(child, QM_XPATH);
            if (StringUtil.hasValue(xpath) && !CONST_IMPL.QM_MAYAA.equals(node.getQName())) {
                // m:mayaa直下でない要素のxpath属性はinjectionに使われない（仕様）
                // ユーザーに意図しない無効化を気づかせるために警告する
                String systemID = child.getSystemID();
                String lineNumber = Integer.toString(child.getLineNumber());
                String msg = StringUtil.getMessage(
                        XPathMatchesInjectionResolver.class, 1,
                        xpath, systemID, lineNumber);
                DiagnosticEventBuffer.recordWarn(
                        DiagnosticEventBuffer.Phase.BUILD,
                        "parseXPathInjection",
                        child.getSystemID(),
                        msg, null, xpath, child);
            }
            warnInvalidXPathNodesRecursive(child);
        }
    }

    protected void logWarnning(String xpath, SpecificationNode node, int number) {
        String systemID = node.getSystemID();
        String lineNumber = Integer.toString(node.getLineNumber());
        String msg = StringUtil.getMessage(
                XPathMatchesInjectionResolver.class, number,
                xpath, systemID, lineNumber);
        DiagnosticEventBuffer.recordWarn(
                DiagnosticEventBuffer.Phase.BUILD,
                "resolveXPathInjection",
                node.getSystemID(),
                msg,
                null,
                xpath,
                node);
    }

    // support class -------------------------------------------------

    protected static class CheckXPathCopyToFilter implements CopyToFilter {

        public boolean accept(NodeObject test) {
            if (test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute) test;
                return attr.getQName().equals(QM_XPATH) == false;
            }
            return true;
        }

    }

}

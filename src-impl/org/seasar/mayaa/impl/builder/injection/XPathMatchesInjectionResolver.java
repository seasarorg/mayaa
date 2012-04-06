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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
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
public class XPathMatchesInjectionResolver extends ParameterAwareImpl
        implements InjectionResolver, CONST_IMPL {

    private static final long serialVersionUID = -3738385077054952571L;
    private static final Log LOG =
        LogFactory.getLog(XPathMatchesInjectionResolver.class);
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
            SpecificationNode node, List specificationNodes) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = node.iterateChildNode(); it.hasNext();) {
            SpecificationNode child = (SpecificationNode) it.next();
            String xpath = SpecificationUtil.getAttributeValue(child, QM_XPATH);
            if (StringUtil.hasValue(xpath)) {
                if (QM_MAYAA.equals(node.getQName())) {
                    specificationNodes.add(child);
                } else {
                    // m:mayaa直下でなければ警告し、利用しない
                    logWarnning(xpath, child, 1);
                }
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
        namespace.addPrefixMapping("m", URI_MAYAA);

        // mayaaファイル内のm:xpathを持つすべてのノードを対象とする
        Specification spec = SpecificationUtil.findSpecification(original);
        List injectNodes = new ArrayList();
        while (spec != null) {
            SpecificationNode mayaa = SpecificationUtil.getMayaaNode(spec);
            if (mayaa != null) {
                getXPathNodes(mayaa, injectNodes);
            }
            spec = EngineUtil.getParentSpecification(spec);
        }

        for (Iterator it = injectNodes.iterator(); it.hasNext();) {
            SpecificationNode injected = (SpecificationNode) it.next();
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

    protected void logWarnning(String xpath, SpecificationNode node, int number) {
        if (LOG.isWarnEnabled()) {
            String systemID = node.getSystemID();
            String lineNumber = Integer.toString(node.getLineNumber());
            String msg = StringUtil.getMessage(
                    XPathMatchesInjectionResolver.class, number,
                    xpath, systemID, lineNumber);
            LOG.warn(msg);
        }
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

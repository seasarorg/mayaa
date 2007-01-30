/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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

import java.util.Iterator;

import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.engine.specification.xpath.XPathUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XPathMatchesInjectionResolver extends ParameterAwareImpl
        implements InjectionResolver, CONST_IMPL {

    private static final long serialVersionUID = -5357509098529015227L;
    protected static final QName QM_XPATH =
        SpecificationUtil.createQName("xpath");

    private CopyToFilter _xpathFilter = new CheckXPathCopyToFilter();

    protected CopyToFilter getCopyToFilter() {
        return _xpathFilter;
    }

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if (original == null || chain == null) {
            throw new IllegalArgumentException();
        }

        // TODO テンプレートのprefix定義、Mayaaファイルのを反映させる
        Namespace namespace = SpecificationUtil.createNamespace();
        namespace.addPrefixMapping("m", URI_MAYAA);
        String xpathExpr = "/m:mayaa//*[string-length(@m:xpath) > 0]";

        // mayaaファイル内のm:xpathを持つすべてのノードを対象とする
        for (Iterator it = XPathUtil.selectChildNodes(
                original, xpathExpr, namespace, true); it.hasNext();) {
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

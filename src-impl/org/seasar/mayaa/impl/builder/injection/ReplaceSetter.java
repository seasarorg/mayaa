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

import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.builder.BuilderUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * テンプレートを見てm:replace="false"がセットされているなら、テンプレート
 * オリジナルのノードを複製してオリジナルノードがそのまま出力されるようにする。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ReplaceSetter extends ParameterAwareImpl
        implements InjectionResolver, CONST_IMPL {

    private static final long serialVersionUID = 442671575099062287L;
    protected static final QName QM_REPLACE =
        SpecificationUtil.createQName("replace");

    protected boolean isReplace(SpecificationNode node) {
        if (node == null) {
            throw new IllegalArgumentException();
        }
        NodeAttribute attr = node.getAttribute(QM_REPLACE);
        if (attr != null) {
            return ObjectUtil.booleanValue(attr.getValue(), true);
        }
        return true;
    }

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if (original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode injected = chain.getNode(original);
        if (injected == null) {
            return null;
        }
        if (isReplace(original) == false || isReplace(injected) == false) {
            QName qName = original.getQName();
            URI uri = qName.getNamespaceURI();
            SpecificationNode element = BuilderUtil.createInjectedNode(
                    QM_DUPLECATED, uri, original, false);
            StringBuffer name = new StringBuffer();
            String prefix = original.getPrefix();
            if (StringUtil.hasValue(prefix)) {
                name.append(prefix).append(":");
            }
            name.append(qName.getLocalName());
            element.addAttribute(QM_NAME, name.toString());
            element.addChildNode(injected);
            return element;
        }
        return injected;
    }

}

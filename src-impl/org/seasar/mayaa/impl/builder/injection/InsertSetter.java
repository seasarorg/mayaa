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
package org.seasar.maya.impl.builder.injection;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.builder.BuilderUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.ProviderUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InsertSetter extends ParameterAwareImpl 
		implements InjectionResolver, CONST_IMPL {

    protected static final QName QM_INSERT = 
        SpecificationUtil.createQName("insert");
    protected static final QName QM_PATH = 
        SpecificationUtil.createQName("path");

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        SpecificationNode injected = chain.getNode(original);
        QName qName = injected.getQName();
        String uri = qName.getNamespaceURI();
        if(uri.startsWith("/")) {
	        LibraryManager libraryManager = ProviderUtil.getLibraryManager();
	        if(libraryManager.getProcessorDefinition(qName) == null) {
	            String name = qName.getLocalName();
	            String path = 
                    StringUtil.preparePath(uri) + StringUtil.preparePath(name);
                SpecificationNode node = BuilderUtil.createInjectedNode(
                        QM_INSERT, uri, injected, false);
                node.addAttribute(QM_PATH, path);
                String compName = SpecificationUtil.getAttributeValue(
                        injected, QM_NAME);
                if(StringUtil.hasValue(compName)) {
                    node.addAttribute(QM_NAME, compName);
                }
                return node;
	        }
        }
        return injected;
    }

}

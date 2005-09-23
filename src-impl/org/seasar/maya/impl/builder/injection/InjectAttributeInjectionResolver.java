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
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.BuilderUtil;
import org.seasar.maya.impl.engine.specification.QNameImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InjectAttributeInjectionResolver 
        implements InjectionResolver, CONST_IMPL {

    protected static final QName QM_INJECT =
        new QNameImpl("inject");

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
    	String injectName = 
            SpecificationUtil.getAttributeValue(original, QM_INJECT);
        if(StringUtil.hasValue(injectName)) {
            QNameable qNameable = 
                BuilderUtil.parseName(original, injectName);
            QName qName = qNameable.getQName();
            if(QM_IGNORE.equals(qName) == false) {
	            String uri = qName.getNamespaceURI();
	            return BuilderUtil.createInjectedNode(
                        qName, uri, original, true); 
            }
        }
        return chain.getNode(original);
    }
    
    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }

}

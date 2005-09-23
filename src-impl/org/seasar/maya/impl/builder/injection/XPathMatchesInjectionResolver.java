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

import java.util.Iterator;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.QNameImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.engine.specification.xpath.XPathUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XPathMatchesInjectionResolver 
        implements InjectionResolver, CONST_IMPL {

    protected static final QName QM_XPATH = new QNameImpl("xpath");
    
    private CopyToFilter _xpathFilter = new CheckXPathCopyToFilter(); 

    protected CopyToFilter getCopyToFilter() {
        return _xpathFilter;
    }
    
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        Namespace namespace = SpecificationUtil.createNamespace();
        namespace.addPrefixMapping("m", URI_MAYA);
        String xpathExpr = "/m:maya//*[string-length(@m:xpath) > 0]";
        for(Iterator it = XPathUtil.selectChildNodes(
                original, xpathExpr, namespace, true); it.hasNext(); ) {
            SpecificationNode injected = (SpecificationNode)it.next();
            String mayaPath = SpecificationUtil.getAttributeValue(
            		injected, QM_XPATH);
            if(XPathUtil.matches(original, mayaPath, injected)) {
                return injected.copyTo(getCopyToFilter());
            }
        }
        return chain.getNode(original);
    }
    
    // Parameterizable implements ------------------------------------
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
    
    // support class -------------------------------------------------
    
    protected class CheckXPathCopyToFilter implements CopyToFilter {
        
        public boolean accept(NodeObject test) {
            if(test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute)test;
                return attr.getQName().equals(QM_XPATH) == false;
            }
            return true;
        }
        
    }
    
}

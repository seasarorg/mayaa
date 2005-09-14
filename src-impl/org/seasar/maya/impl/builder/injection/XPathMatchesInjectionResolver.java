/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.builder.injection;

import java.util.Iterator;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.NamespaceImpl;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.engine.specification.XPathUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XPathMatchesInjectionResolver 
        implements InjectionResolver, CONST_IMPL {

    private final CheckXPathCopyToFilter _xpathFilter = 
        new CheckXPathCopyToFilter(); 
    
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        Namespace namespace = new NamespaceImpl();
        namespace.addPrefixMapping("m", URI_MAYA);
        String xpathExpr = "/m:maya//*[string-length(@m:xpath) > 0]";
        for(Iterator it = XPathUtil.selectChildNodes(
                original, xpathExpr, namespace, true); it.hasNext(); ) {
            SpecificationNode injected = (SpecificationNode)it.next();
            String mayaPath = SpecificationUtil.getAttributeValue(
            		injected, QM_XPATH);
            if(XPathUtil.matches(original, mayaPath, injected)) {
                return injected.copyTo(_xpathFilter);
            }
        }
        return chain.getNode(original);
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }
    
    private class CheckXPathCopyToFilter implements CopyToFilter {
        
        public boolean accept(NodeObject test) {
            if(test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute)test;
                return attr.getQName().equals(QM_XPATH) == false;
            }
            return true;
        }
        
    }
    
}

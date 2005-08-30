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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.builder.injection.InjectionChain;
import org.seasar.maya.builder.injection.InjectionResolver;
import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.XPathUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XPathMatchesInjectionResolver 
        implements InjectionResolver, CONST_IMPL {

    private final CheckXPathCopyToFilter _xpathFilter = 
        new CheckXPathCopyToFilter(); 
    
	private Map getNamespaceMap(SpecificationNode node) {
		Map namespaces  = new HashMap();
        for(Iterator nsit = node.iterateNamespace(); nsit.hasNext(); ) {
        	NodeNamespace ns = (NodeNamespace)nsit.next();
        	namespaces.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return namespaces;
	}
	
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        Map namespaces = new HashMap();
        namespaces.put("m", URI_MAYA);
        String xpathExpr = "/m:maya//*[string-length(@m:xpath) > 0]";
        for(Iterator it = XPathUtil.selectChildNodes(
                original, xpathExpr, namespaces, true); it.hasNext(); ) {
            SpecificationNode injected = (SpecificationNode)it.next();
            String mayaPath = SpecificationUtil.getAttributeValue(injected, QM_XPATH);
            if(XPathUtil.matches(original, mayaPath, getNamespaceMap(injected))) {
                if(QM_IGNORE.equals(injected)) {
                    return chain.getNode(original);
                }
                return injected.copyTo(_xpathFilter);
            }
        }
        return chain.getNode(original);
    }
    
    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
    
    private class CheckXPathCopyToFilter implements CopyToFilter {
        
        public boolean accept(NodeObject test) {
            if(test instanceof SpecificationNode) {
                SpecificationNode node = (SpecificationNode)test;
                String xpath = SpecificationUtil.getAttributeValue(node, QM_XPATH);
                return StringUtil.isEmpty(xpath);
            }
            return true;
        }
        
    }
    
}

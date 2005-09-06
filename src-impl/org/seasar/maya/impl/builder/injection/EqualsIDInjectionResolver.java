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
import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.library.ProcessorDefinition;
import org.seasar.maya.engine.specification.CopyToFilter;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeObject;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.specification.NamespaceableImpl;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.XPathUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EqualsIDInjectionResolver
        implements InjectionResolver, CONST_IMPL {

    private static LibraryManager _libraryManager;    
    
    private CheckIDCopyToFilter _idFilter = new CheckIDCopyToFilter();

    private String getID(SpecificationNode node) {
	    if(node == null) {
	        throw new IllegalArgumentException();
	    }
    	NodeAttribute attr = node.getAttribute(QM_ID);
    	if(attr != null) {
    		return attr.getValue();
    	}
		attr = node.getAttribute(QX_ID);
		if(attr != null) {
			return attr.getValue();
		}
		attr = node.getAttribute(QH_ID);
		if(attr != null) {
			return attr.getValue();
		}
		return null;
    }

    protected static SpecificationNode checkNode(
            SpecificationNode injected, CopyToFilter filter) {
        if(_libraryManager == null) {
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            _libraryManager = provider.getTemplateBuilder().getLibraryManager();
        }
        QName name = injected.getQName();
        ProcessorDefinition def = _libraryManager.getProcessorDefinition(name);
        if(def != null) {
            return injected.copyTo(filter);
        }
        InjectionChain chain = DefaultInjectionChain.getInstance(); 
        return chain.getNode(injected); 
    }
    
    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        String id = getID(original);
        if(StringUtil.hasValue(id)) {
            Namespaceable namespaceable = new NamespaceableImpl();
            namespaceable.addNamespace("m", URI_MAYA);
            String xpathExpr = "/m:maya//*[@m:id='" + id + "']"; 
            Iterator it = XPathUtil.selectChildNodes(
                    original, xpathExpr, namespaceable, true);
	        if(it.hasNext()) {
	            SpecificationNode injected = (SpecificationNode)it.next();
	            if(QM_IGNORE.equals(injected.getQName())) {
	                return chain.getNode(original);
	            }
	            return checkNode(injected, _idFilter);
	        }
            boolean report = SpecificationUtil.getEngineSettingBoolean(
                    REPORT_UNRESOLVED_ID, true);
            if(report) { 
		        throw new IDNotResolvedException(id);
            }
        }
        return chain.getNode(original);
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
    
    private class CheckIDCopyToFilter implements CopyToFilter {
   
        public boolean accept(NodeObject test) {
            if(test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute)test;
                return attr.getQName().equals(QM_ID) == false;
            }
            return true;
        }
        
    }
    
}

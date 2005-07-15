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
package org.seasar.maya.impl.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.Namespaceable;
import org.seasar.maya.engine.specification.NodeAttribute;
import org.seasar.maya.engine.specification.NodeNamespace;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.QNameable;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.IllegalNameException;
import org.seasar.maya.impl.builder.PrefixMappingNotFoundException;
import org.seasar.maya.impl.engine.specification.QNameableImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.provider.ModelProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.xml.sax.Locator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationUtil implements CONST_IMPL {
    
	private SpecificationUtil() {
	}

    public static String getAttributeValue(SpecificationNode node, QName qName) {
        NodeAttribute nameAttr = node.getAttribute(qName);
        if(nameAttr != null) {
        	return nameAttr.getValue();
        }
        return null;
    }
    
    public static String findAttributeValue(Specification specification, QName qName) {
        while(specification != null) {
	        String value = getAttributeValue(specification, qName);
	        if(value != null) {
	            return value;
	        }
	        specification = specification.getParentSpecification();
        }
        return null;
    }
    
    public static Class getModelClass(SpecificationNode node) {
        NodeAttribute classAttr = node.getAttribute(QM_CLASS);
        if(classAttr != null) {
	        String className = classAttr.getValue();
            return ObjectUtil.loadClass(className);
        }
        return null;
    }
    
    public static String getModelScope(SpecificationNode node) {
        NodeAttribute scopeAttr = node.getAttribute(QM_SCOPE);
        if(scopeAttr != null) {
            return scopeAttr.getValue();
        }
        return ServiceCycle.SCOPE_PAGE;
    }
    
    /**
     * コンテキストから、より下位のSpecificationを探す。見つからないとnullを返す。
     * @param context カレントのコンテキスト。
     * @return みつかったSpecification（テンプレート/ページ/エンジン）。
     */
    public static Specification findSpecification(ServiceCycle cycle) {
    	Specification specification = (Template)cycle.getAttribute(KEY_TEMPLATE);
        if(specification == null) {
            specification = (Page)cycle.getAttribute(KEY_PAGE);
            if(specification == null) {
                specification = (Engine)cycle.getAttribute(KEY_ENGINE);
        	}
        }
        return specification;
    }
    
    public static Object findSpecificationModel(
            ServiceCycle cycle, Specification specification) {
        if(cycle == null) {
            throw new IllegalArgumentException();
        }
        while(specification != null) {
            SpecificationNode maya = getMayaNode(specification);
            if(maya != null) {
		        Class modelClass = getModelClass(maya);
		        if(modelClass != null) {
		            String name = specification.getKey();
		            String scope = SpecificationUtil.getModelScope(maya);
		            ModelProvider provider = ServiceProviderFactory.getModelProvider();
		           	return provider.getModel(cycle, name, modelClass, scope);
		        }
            }
            specification = specification.getParentSpecification();
        }
        return null;
    }
	
	public static SpecificationNode getMayaNode(Specification specification) {
	    Map namespaces = new HashMap();
	    namespaces.put("m", URI_MAYA);
	    Iterator it = specification.selectChildNodes("/m:maya", namespaces, false);
	    if(it.hasNext()) {
	        return (SpecificationNode)it.next();
	    }
	    return null;
	}
	
	// FIXME 引数の整理： Specification、locatorは例外にしか使ってない。
    public static QNameable parseName(Namespaceable namespaces, 
            Specification specification, Locator locator, String qName, String defaultURI) {
        String[] parsed = qName.split(":");
        String prefix = null;
        String localName = null;
        String namespaceURI = null;
        if(parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            NodeNamespace namespace = namespaces.getNamespace(prefix);
            if(namespace == null) {
                throw new PrefixMappingNotFoundException(specification, locator, prefix);
            }
            namespaceURI = namespace.getNamespaceURI();
        } else if(parsed.length == 1) {
            localName = parsed[0];
            namespaceURI = defaultURI;
        } else {
            throw new IllegalNameException(specification, locator, qName);
        }
        return new QNameableImpl(new QName(namespaceURI, localName));
    }
    
    public static SpecificationNode createInjectedNode(
            QName qName, String uri, SpecificationNode original) {
        SpecificationNodeImpl node =  new SpecificationNodeImpl(qName, original.getLocator());
        for(Iterator it = original.iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            if(uri.equals(attr.getQName().getNamespaceURI())) {
                node.addAttribute(attr.getQName(), attr.getValue());
            }
        }
        return node;
    }
	
}

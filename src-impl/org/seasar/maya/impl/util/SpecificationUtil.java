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

import java.util.Iterator;

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
import org.seasar.maya.impl.engine.specification.NamespaceableImpl;
import org.seasar.maya.impl.engine.specification.QNameableImpl;
import org.seasar.maya.impl.engine.specification.SpecificationNodeImpl;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;

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

    public static Specification findSpecification(SpecificationNode current) {
        while(current instanceof Specification == false) {
            current = current.getParentNode();
            if(current == null) {
                return null;
            }
        }
        return (Specification)current;
    }
    
    public static Specification findSpecification() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        SpecificationNode current = cycle.getOriginalNode();
        return findSpecification(current);
    }

    public static Template getTemplate() {
        Specification spec = findSpecification();
        if(spec instanceof Page) {
            spec = findSpecification(spec.getParentNode());
        }
        if(spec instanceof Template) {
            return (Template)spec;
        }
        throw new IllegalStateException();
    }
    
	public static SpecificationNode getMayaNode(SpecificationNode node) {
        Specification specification = findSpecification(node);
        Namespaceable namespaceable = new NamespaceableImpl();
        namespaceable.addNamespace("m", URI_MAYA);
	    Iterator it = XPathUtil.selectChildNodes(
                specification, "/m:maya", namespaceable, false);
	    if(it.hasNext()) {
	        return (SpecificationNode)it.next();
	    }
	    return null;
	}
    
    private static Class getModelClass(SpecificationNode node) {
        NodeAttribute classAttr = node.getAttribute(QM_CLASS);
        if(classAttr != null) {
            String className = classAttr.getValue();
            return ObjectUtil.loadClass(className);
        }
        return null;
    }
    
    private static String getModelScope(SpecificationNode node) {
        NodeAttribute scopeAttr = node.getAttribute(QM_SCOPE);
        if(scopeAttr != null) {
            return scopeAttr.getValue();
        }
        return ServiceCycle.SCOPE_PAGE;
    }

    public static Object findSpecificationModel(Specification specification) {
        while (specification != null) {
            SpecificationNode maya = getMayaNode(specification);
            if (maya != null) {
                Class modelClass = getModelClass(maya);
                if (modelClass != null) {
                    String scope = SpecificationUtil.getModelScope(maya);
                    ServiceProvider provider = ProviderFactory
                            .getServiceProvider();
                    return provider.getModel(modelClass, scope);
                }
            }
            specification = specification.getParentSpecification();
        }
        return null;
    }
	
    public static QNameable parseName(
            Namespaceable namespaces, String qName) {
        String[] parsed = qName.split(":");
        String prefix = null;
        String localName = null;
        String namespaceURI = null;
        if(parsed.length == 2) {
            prefix = parsed[0];
            localName = parsed[1];
            NodeNamespace namespace = namespaces.getNamespace(prefix, true);
            if(namespace == null) {
                throw new PrefixMappingNotFoundException(prefix);
            }
            namespaceURI = namespace.getNamespaceURI();
        } else if(parsed.length == 1) {
            localName = parsed[0];
            NodeNamespace namespace = namespaces.getNamespace("", true);
            if(namespace != null) {
                namespaceURI = namespace.getNamespaceURI();
            } else {
                throw new PrefixMappingNotFoundException("");
            }
        } else {
            throw new IllegalNameException(qName);
        }
        QName retName = new QName(namespaceURI, localName);
        QNameable ret = new QNameableImpl(retName);
        ret.setParentScope(namespaces);
        return ret;
    }
    
    public static SpecificationNode createInjectedNode(
            QName qName, String uri, SpecificationNode original) {
        SpecificationNodeImpl node = new SpecificationNodeImpl(
        		qName, original.getSystemID(), original.getLineNumber());
        for(Iterator it = original.iterateAttribute(); it.hasNext(); ) {
            NodeAttribute attr = (NodeAttribute)it.next();
            if(uri.equals(attr.getQName().getNamespaceURI())) {
                node.addAttribute(attr.getQName(), attr.getValue());
            }
        }
        node.setParentScope(original.getParentScope());
        return node;
    }
	
    public static Engine getEngine() {
        ServiceProvider provider = ProviderFactory.getServiceProvider();
        return provider.getEngine();
    }
    
    public static String getEngineSetting(String name, String defaultValue) {
        Engine engine = getEngine();
        String value = engine.getParameter(name);
        if(value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public static boolean getEngineSettingBoolean(String name, boolean defaultValue) {
        Engine engine = getEngine();
        String value = engine.getParameter(name);
        return ObjectUtil.booleanValue(value, defaultValue);
    }
    
}

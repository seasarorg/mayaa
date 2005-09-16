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
package org.seasar.maya.impl.engine.specification.xpath;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.SimpleVariableContext;
import org.jaxen.XPath;
import org.jaxen.XPathFunctionContext;
import org.jaxen.pattern.Pattern;
import org.jaxen.pattern.PatternParser;
import org.jaxen.saxpath.SAXPathException;
import org.seasar.maya.engine.specification.Namespace;
import org.seasar.maya.engine.specification.Specification;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.engine.specification.SpecificationUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class XPathUtil {

    private XPathUtil() {
    }

    public static boolean matches(SpecificationNode test, 
            String xpathExpr, Namespace namespace) {
        if(StringUtil.isEmpty(xpathExpr)) {
            throw new IllegalArgumentException();
        }
        NamespaceContext nsContext;
        if(namespace == null) {
            nsContext = new SimpleNamespaceContext();
        } else {
            nsContext = new NamespaceContextImpl(namespace);
        }
        ContextSupport support = new ContextSupport(
                nsContext,
                XPathFunctionContext.getInstance(),
                new SimpleVariableContext(),
                SpecificationNavigator.getInstance());
        Context context = new Context(support);
        try {
            Pattern pattern = PatternParser.parse(xpathExpr);
            return pattern.matches(test, context);
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        } catch(SAXPathException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Iterator selectChildNodes(SpecificationNode node,
            String xpathExpr, Namespace namespaceable, boolean cascade) {
        Specification specification = SpecificationUtil.findSpecification(node);
        if(StringUtil.isEmpty(xpathExpr)) {
            throw new IllegalArgumentException();
        }
        if(cascade) {
            return new CascadeSelectNodesIterator(
                    specification, xpathExpr, namespaceable);
        }
        try {
            XPath xpath = SpecificationXPath.createXPath(xpathExpr, namespaceable);
            return xpath.selectNodes(specification).iterator();
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }

    private static class CascadeSelectNodesIterator implements Iterator {

        private Specification _specification;
        private String _xpathExpr;
        private Namespace _namespaceable;
        private Iterator _iterator;
        
        private CascadeSelectNodesIterator(Specification specification, 
                String xpathExpr, Namespace namespaceable) {
            if(specification == null || StringUtil.isEmpty(xpathExpr)) {
                throw new IllegalArgumentException();
            }
            _specification = specification;
            _xpathExpr = xpathExpr;
            _namespaceable = namespaceable;
        }
        
        public boolean hasNext() {
            while(true) {
                if(_iterator == null) {
                    XPath xpath = SpecificationXPath.createXPath(
                            _xpathExpr, _namespaceable);
                    try {
                        _iterator = xpath.selectNodes(_specification).iterator();
                    } catch (JaxenException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(_iterator.hasNext()) {
                    return true;
                }
                Specification parent = 
                    EngineUtil.getParentSpecification(_specification);
                if(parent == null) {
                    return false;
                }
                _specification = parent;
                _iterator = null;
            }
        }
        
        public Object next() {
            if(hasNext()) {
                return _iterator.next();
            }
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}

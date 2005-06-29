/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.engine.specification;

import java.util.Map;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.XPath;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationXPath extends BaseXPath {

	private static final long serialVersionUID = -4676164886549672326L;

	public static XPath createXPath(String xpathExpr, Map namespaces) {
        try {
            XPath xpath = new SpecificationXPath(xpathExpr);
            if(namespaces != null) {
                xpath.setNamespaceContext(new SimpleNamespaceContext(namespaces));
            }
            return xpath;
        } catch(JaxenException e) {
            throw new RuntimeException(e);
        }
    }
    
    private SpecificationXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, SpecificationNavigator.getInstance());
    }
    
}

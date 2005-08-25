/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.builder;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.MayaException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NodeNotResolvedException extends MayaException {

	private static final long serialVersionUID = 4560512867759109674L;

	private Template _template;
	private SpecificationNode _specificationNode;
	
	public NodeNotResolvedException(
	        Template template, SpecificationNode specificationNode) {
	    _template = template;
	    _specificationNode = specificationNode;
    }

	public Template getTemplete() {
	    return _template;
	}
	
	public SpecificationNode getSpecificationNode() {
		return _specificationNode;
	}

	public int getColumnNumber() {
		return _specificationNode.getLocator().getColumnNumber();
	}
	
	public int getLineNumber() {
		return _specificationNode.getLocator().getLineNumber();
	}
	
	public String getPublicID() {
		return _specificationNode.getLocator().getPublicId();
	}
	
	public String getSystemID() {
		return _specificationNode.getLocator().getSystemId();
	}

    protected Object[] getMessageParams() {
        return new Object[] { _template, getSpecificationNode(), 
                new Integer(getColumnNumber()), new Integer(getLineNumber()),
                getPublicID(), getSystemID() };
    }
    
}

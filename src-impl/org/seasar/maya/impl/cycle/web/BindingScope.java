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
package org.seasar.maya.impl.cycle.web;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.engine.processor.ComponentPageProcessor;
import org.seasar.maya.impl.util.SpecificationUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class BindingScope extends ParamScope {
	
	public BindingScope(HttpServletRequest request) {
		super(request);
	}

	private ComponentPageProcessor getComponentPageProcessor() {
		Template template = SpecificationUtil.getTemplate();
		TemplateProcessor parent = template.getParentProcessor();
		if (parent == null) {
			return null;
		} else if (parent instanceof ComponentPageProcessor) {
			return (ComponentPageProcessor)parent;
		} else {
			throw new IllegalStateException();
		}
	}
	
	public String getScopeName() {
		return "binding";
	}

	public Iterator iterateAttributeNames() {
		ComponentPageProcessor processor = getComponentPageProcessor(); 
		if(processor != null) {
			return new ComponentBindingIterator(
					processor.getInformalProperties().iterator());
		}
		return super.iterateAttributeNames();
	}

	public Object getAttribute(String name) {
		ComponentPageProcessor processor = getComponentPageProcessor(); 
		for(Iterator it = processor.getInformalProperties().iterator(); it.hasNext(); ) {
			ProcessorProperty prop = (ProcessorProperty)it.next();
			if(prop.getQName().getLocalName().equals(name)) {
				return prop.getValue();
			}
		}
		return super.getAttribute(name);
	}
	
	private class ComponentBindingIterator implements Iterator {
		
		private Iterator _it;
		
		private ComponentBindingIterator(Iterator it) {
			if(it == null) {
				throw new IllegalArgumentException();
			}
			_it = it;
		}

		public boolean hasNext() {
			return _it.hasNext();
		}

		public Object next() {
			ProcessorProperty prop = (ProcessorProperty)_it.next();
			return prop.getQName().getLocalName();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}

}

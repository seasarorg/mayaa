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
package org.seasar.maya.impl.cycle.script.resolver;

import java.util.Iterator;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.engine.processor.ComponentPageProcessor;
import org.seasar.maya.impl.util.collection.AbstractAttributeMap;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 * @deprecated
 */
public class ComponentBindingMap extends AbstractAttributeMap {

	private ComponentPageProcessor _processor;
	
	public ComponentBindingMap(ComponentPageProcessor processor) {
		if(processor == null) {
			throw new IllegalArgumentException();
		}
		_processor = processor;
	}
	
	protected Object getAttribute(String key) {
		for(Iterator it = _processor.getInformalProperties().iterator(); it.hasNext(); ) {
			ProcessorProperty prop = (ProcessorProperty)it.next();
			if(prop.getQName().getLocalName().equals(key)) {
				return prop.getValue();
			}
		}
		return null;
	}

	protected Iterator getAttributeNames() {
		return new ComponentBindingIterator(_processor.getInformalProperties().iterator());
	}

	protected void setAttribute(String key, Object value) {
		throw new UnsupportedOperationException();
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

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
package org.seasar.maya.standard.engine.processor;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class BeanPropertiesProcessor extends AbstractIteratorProcessor {
    
	private static final long serialVersionUID = 8758394579418426008L;
	private String _ignore;
    
    public void setBean(ProcessorProperty bean) {
    	if(bean == null) {
    		throw new IllegalArgumentException();
    	}
    	setExpression(bean);
    }
    
    public void setVar(String var) {
    	super.setVar(var);
    }
    
    public void setIndex(String index) {
    	super.setIndex(index);
    }
    
    public void setIgnore(String ignore) {
    	_ignore = ignore;
    }
    
    protected Iterator createIterator(PageContext context, Object eval) {
        if(context == null || eval == null) {
            throw new IllegalArgumentException();
        }
        PropertyDescriptor[] descs = PropertyUtils.getPropertyDescriptors(eval);
        if(descs != null) {
            return new BeanPropertiesIterator(descs, eval, _ignore);
        }
        return null;
    }

    private class BeanPropertiesIterator implements Iterator {

        private PropertyDescriptor[] _array;
        private Object _bean;
        private Pattern[] _ignore;
        private int _count;
        private int _index;
        
        private BeanPropertiesIterator(PropertyDescriptor[] array, Object bean, String ignore) {
            if(array == null || bean == null) {
                throw new IllegalArgumentException();
            }
            _array = array;
            _bean = bean;
            if(StringUtil.hasValue(ignore)) {
                String[] patterns = ignore.split(",");
                _ignore = new Pattern[patterns.length];
            	for (int i = 0; i < _ignore.length; ++i) {
            	    _ignore[i] = Pattern.compile(patterns[i].trim());
            	}
            } else {
                _ignore = new Pattern[0];
            }
            _count = array.length;
        }

    	private boolean isIgnore(String propertyName) {
    		for (int i = 0; i < _ignore.length; ++i) {
    			if (_ignore[i].matcher(propertyName).matches()) {
    				return true;
    			}
    		}
    		return false;
    	}
        
        private int getNextIndex() {
            for(int i = _index; i < _count; i++) { 
    	        String propertyName = _array[i].getName();
    	        if(isIgnore(propertyName)) {
    	            continue;
    	        }
                return i;
            }
            return _count;
        }
        
        public boolean hasNext() {
            if(_index < _count) {
                return getNextIndex() < _count;
            }
            return false;
        }

        private Map getVarMap(int index) {
            PropertyDescriptor desc = _array[index];
            Map map = new HashMap();
            map.put("name", desc.getName());
            map.put("type", desc.getPropertyType());
            Object value = null;
            try {
                String propertyName = desc.getName();
                value = PropertyUtils.getProperty(_bean, propertyName);
            } catch (Exception ignore) {
            }
            map.put("value", value);
            return map;
        }
        
        public Object next() {
            if(hasNext()) {
                _index = getNextIndex();
                return getVarMap(_index++);
            }
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}

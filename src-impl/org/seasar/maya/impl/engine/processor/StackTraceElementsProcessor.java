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
package org.seasar.maya.impl.engine.processor;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.seasar.maya.engine.processor.ProcessorProperty;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class StackTraceElementsProcessor extends AbstractIteratorProcessor {
    
	private static final long serialVersionUID = 2403694362873136701L;

	public void setThrowable(ProcessorProperty throwable) {
    	if(throwable == null) {
    		throw new IllegalArgumentException();
    	}
        setProperty(throwable);
    }
    
    public void setVar(String var) {
    	super.setVar(var);
    }
    
    public void setIndex(String index) {
    	super.setIndex(index);
    }
    
    protected Iterator createIterator(Object expValue) {
        if(expValue instanceof Throwable == false) {
            throw new IllegalArgumentException();
        }
        StackTraceElement[] elements = ((Throwable)expValue).getStackTrace();
        if(elements != null) {
            return new StackTraceElementsIterator(elements);
        }
        return null;
    }

    private class StackTraceElementsIterator implements Iterator {

        private StackTraceElement[] _array;
        private int _count;
        private int _index;
        
        private StackTraceElementsIterator(StackTraceElement[] array) {
            if(array == null) {
                throw new IllegalArgumentException();
            }
            _array = array;
            _count = array.length;
        }

        public boolean hasNext() {
            return _index < _count;
        }

        public Object next() {
            if(hasNext()) {
                return _array[_index++];
            }
            throw new NoSuchElementException();
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }

}

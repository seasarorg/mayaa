/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.maya.impl.util.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NullIterator implements Iterator {

    private static final NullIterator _nullIterator = new NullIterator();
    
    public static final Iterator getInstance() {
        return _nullIterator;
    }
    
    private NullIterator() {
    }
    
    public boolean hasNext() {
        return false;
    }
    
    public Object next() {
        throw new NoSuchElementException();
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
}

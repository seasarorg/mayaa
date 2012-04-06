/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.util.collection;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class LIFOIterator implements Iterator {

    private Stack _stack = new Stack();

    public LIFOIterator() {
        // do nothing.
    }

    public LIFOIterator(Iterator it) {
        if (it == null) {
            throw new IllegalArgumentException();
        }
        while (it.hasNext()) {
            add(it.next());
        }
    }

    public LIFOIterator(Enumeration enumeration) {
        if (enumeration == null) {
            throw new IllegalArgumentException();
        }
        while (enumeration.hasMoreElements()) {
            add(enumeration.nextElement());
        }
    }

    public void add(Object item) {
        if (item == null) {
            throw new IllegalArgumentException();
        }
        _stack.push(item);
    }

    public boolean hasNext() {
        return _stack.size() > 0;
    }

    public Object next() {
        return _stack.pop();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

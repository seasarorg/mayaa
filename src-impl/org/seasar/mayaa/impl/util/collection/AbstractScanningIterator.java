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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractScanningIterator implements Iterator {

    private Stack _stack;

    private Object _next;

    public AbstractScanningIterator(Iterator iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException();
        }
        _stack = new Stack();
        _stack.push(iterator);
    }

    protected boolean filter(Object test) {
        return true;
    }

    protected Object getNextObject(Object next) {
        return next;
    }

    public boolean hasNext() {
        while (true) {
            if (_next != null) {
                return true;
            }
            Iterator it = (Iterator) _stack.peek();
            if (it.hasNext()) {
                Object next = getNextObject(it.next());
                if (next instanceof Iterator) {
                    _stack.push(next);
                } else if (filter(next)) {
                    _next = next;
                }
            } else {
                if (_stack.size() > 1) {
                    _stack.pop();
                } else {
                    return false;
                }
            }
        }
    }

    public Object next() {
        if (_next == null) {
            hasNext();
        }
        if (_next == null) {
            throw new NoSuchElementException();
        }
        Object ret = _next;
        _next = null;
        return ret;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

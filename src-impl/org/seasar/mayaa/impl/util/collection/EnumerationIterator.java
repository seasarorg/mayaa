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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EnumerationIterator<T> implements Iterator<T> {

    private Enumeration<T> _enumration;

    public static <T> Iterator<T> getInstance(Enumeration<T> enumration) {
        if (enumration == null) {
            return Collections.emptyIterator();
        }
        if (enumration instanceof IteratorEnumeration) {
            return ((IteratorEnumeration<T>) enumration).getInternalIterator();
        }
        return new EnumerationIterator<T>(enumration);
    }

    private EnumerationIterator(Enumeration<T> enumration) {
        if (enumration == null) {
            throw new IllegalArgumentException();
        }
        _enumration = (Enumeration<T>) enumration;
    }

    public Enumeration<T> getInternalEnumeration() {
        return _enumration;
    }

    public boolean hasNext() {
        return _enumration.hasMoreElements();
    }

    public T next() {
        return _enumration.nextElement();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}

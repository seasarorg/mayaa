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
public class IteratorEnumeration<T> implements Enumeration<T> {

    private Iterator<T> _iterator;

    public static <T> Enumeration<T> getInstance(Iterator<T> iterator) {
        if (iterator == null) {
            return Collections.emptyEnumeration();
        }
        if (iterator instanceof EnumerationIterator) {
            return ((EnumerationIterator<T>) iterator).getInternalEnumeration();
        }
        return new IteratorEnumeration<>(iterator);
    }

    private IteratorEnumeration(Iterator<T> iterator) {
        if (iterator == null) {
            throw new IllegalArgumentException();
        }
        _iterator = iterator;
    }

    public Iterator<T> getInternalIterator() {
        return _iterator;
    }

    public boolean hasMoreElements() {
        return _iterator.hasNext();
    }

    public T nextElement() {
        return _iterator.next();
    }

}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.seasar.mayaa.builder.library.scanner.SourceScanner;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class IteratorIterator implements Iterator {

    private List _iterators = new ArrayList();
    private Iterator _iteratorIterator;
    private Iterator _currentIterator;

    protected void check(Object o) {
        if (o == null) {
            throw new IllegalArgumentException();
        }
        if (_iteratorIterator != null) {
            throw new IllegalStateException();
        }
    }

    public void add(Collection collection) {
        check(collection);
        _iterators.add(collection);
    }

    public void add(Enumeration enumeration) {
        check(enumeration);
        _iterators.add(enumeration);
    }

    public void add(SourceScanner scanner) {
        check(scanner);
        _iterators.add(scanner);
    }

    public void add(Iterator iterator) {
        check(iterator);
        _iterators.add(iterator);
    }

    public void remove() {
        if (_currentIterator == null) {
            throw new IllegalStateException();
        }
        _currentIterator.remove();
    }

    public boolean hasNext() {
        if (_currentIterator == null
                || _currentIterator.hasNext() == false) {
            _currentIterator = iteratorNext();
            if (_currentIterator == NullIterator.getInstance()) {
                return false;
            }
            return hasNext();
        }
        return true;
    }

    public Object next() {
        if (hasNext() == false) {
            throw new NoSuchElementException();
        }
        return _currentIterator.next();
    }

    public Iterator iteratorNext() {
        if (_iteratorIterator == null) {
            _iteratorIterator = _iterators.iterator();
        }
        if (_iteratorIterator.hasNext() == false) {
            return NullIterator.getInstance();
        }
        Object o = _iteratorIterator.next();
        if (o instanceof Collection) {
            return ((Collection)o).iterator();
        } else if (o instanceof Enumeration) {
            return EnumerationIterator.getInstance((Enumeration)o);
        } else if (o instanceof SourceScanner) {
            return ((SourceScanner)o).scan();
        }
        return (Iterator)o;
    }
}


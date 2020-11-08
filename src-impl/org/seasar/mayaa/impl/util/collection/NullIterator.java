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

/**
 * Iterator用のNullObject。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @deprecated 1.2 以降で廃止予定
 */
@SuppressWarnings("rawtypes")
public class NullIterator implements Iterator {

    private static final NullIterator NULL_ITERATOR = new NullIterator();

    public static final Iterator getInstance() {
        return NULL_ITERATOR;
    }

    private NullIterator() {
        // singleton.
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

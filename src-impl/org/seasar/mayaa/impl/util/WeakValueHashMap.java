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
package org.seasar.mayaa.impl.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * see http://www.roseindia.net/javatutorials/implementing_softreference_based_hashmap.shtml
 * @author Heinz
 * @author Taro Kato (Gluegent, Inc.)
 */
public class WeakValueHashMap extends AbstractMap {
    /** The internal HashMap that will hold the SoftReference. */
    private final Map hash = new HashMap();
    /** The number of "hard" references to hold internally. */
    private final int HARD_SIZE;
    /** The FIFO list of hard references, order of last access. */
    private final LinkedList hardCache = new LinkedList();
    /** Reference queue for cleared SoftReference objects. */
    private final ReferenceQueue queue = new ReferenceQueue();

    public WeakValueHashMap() {
        this(100);
    }

    public WeakValueHashMap(int hardSize) {
        HARD_SIZE = hardSize;
    }

    public Object get(Object key) {
        Object result = null;
        // We get the SoftReference represented by that key
        WeakValue soft_ref = (WeakValue) hash.get(key);
        if (soft_ref != null) {
            // From the SoftReference we get the value, which can be
            // null if it was not in the map, or it was removed in
            // the processQueue() method defined below
            result = soft_ref.get();
            if (result == null) {
                // If the value has been garbage collected, remove the
                // entry from the HashMap.
                hash.remove(key);
            } else {
                // We now add this object to the beginning of the hard
                // reference queue. One reference can occur more than
                // once, because lookups of the FIFO queue are slow, so
                // we don't want to search through it each time to remove
                // duplicates.
                hardCache.addFirst(result);
                if (hardCache.size() > HARD_SIZE) {
                    // Remove the last entry if list longer than HARD_SIZE
                    hardCache.removeLast();
                }
            }
        }
        return result;
    }

    /**
     * We define our own subclass of SoftReference which contains not only the value but also the key to make
     * it easier to find the entry in the HashMap after it's been garbage collected.
     */
    private static class WeakValue extends WeakReference {
        final Object key; // always make data member final

        /**
         * Did you know that an outer class can access private data members and methods of an inner class? I
         * didn't know that! I thought it was only the inner class who could access the outer class's private
         * information. An outer class can also access private members of an inner class inside its inner
         * class.
         * @param k
         * @param key
         * @param q
         */
        protected WeakValue(Object k, Object key, ReferenceQueue q) {
            super(k, q);
            this.key = key;
        }
    }

    /**
     * Here we go through the ReferenceQueue and remove garbage collected SoftValue objects from the HashMap
     * by looking them up using the SoftValue.key data member.
     */
    private void processQueue() {
        WeakValue wv;
        while ((wv = (WeakValue) queue.poll()) != null) {
            hash.remove(wv.key); // we can access private data!
        }
    }

    /**
     * Here we put the key, value pair into the HashMap using a SoftValue object.
     * @param key
     * @param value
     * @return Object
     */
    public Object put(Object key, Object value) {
        processQueue(); // throw out garbage collected values first
        return hash.put(key, new WeakValue(value, key, queue));
    }

    public Object remove(Object key) {
        processQueue(); // throw out garbage collected values first
        return hash.remove(key);
    }

    public void clear() {
        hardCache.clear();
        processQueue(); // throw out garbage collected values
        hash.clear();
    }

    public int size() {
        processQueue(); // throw out garbage collected values first
        return hash.size();
    }

    public Set entrySet() {
        // no, no, you may NOT do that!!! GRRR
        throw new UnsupportedOperationException();
    }
}

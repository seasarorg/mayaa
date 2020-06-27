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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.collections.map.AbstractReferenceMap;

/**
 * オブジェクトキャッシュ用に指定した件数を強参照で保持して残りは弱参照で保持するマップ。
 * 強参照で保持する件数を超えた場合はLRUで弱参照に移動させる。
 * 
 * @author Watanabe, Mitsutaka (Re-implemented)
 */
public class WeakValueHashMap<K, V> {

    /** 強参照で保持させるデフォルトの件数。 */
    private static final int DEFAULT_HARD_SIZE = 128;

    /** キャッシュレコードを強参照で保持するマップ。指定した件数を超える場合はLRUで破棄する。 */
    private final LruHashMap _hardReferenceLruMap;

    /**
     * キャッシュレコードを弱参照で保持するマップ。LRUで追い出されたレコードを保持する。
     * このマップに保持されているレコードが再び参照された場合は改めて強参照のマップに移動させる。
     */
    @SuppressWarnings("unchecked")
    private final Map<K, CountedReference<V>> _weakReferenceMap = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK, true);


    /** 同期化オブジェクト */
    private final Object _mutex = new Object();

    private long _droppedCount = 0;
    private long _pulledUpCount = 0;
    private long _maxCountOfDroppedRecord = 0;

    /**
     * 参照カウンタ付きのキャッシュレコード保持
     */
    private static class CountedReference<V> {
        private final V referent;
        private int count = 0;

        CountedReference(V referent) {
            this.referent = referent;
        }

        V get() {
            return referent;
        }

        int getCount() {
            return count;
        }

        void countUp() {
            ++count;
        }
    }

    private class LruHashMap extends LinkedHashMap<K, CountedReference<V>> {

        private static final long serialVersionUID = 8437986358875751211L;

        private int maxSize;

        public LruHashMap(int maxSize) {
            super(maxSize, 0.90f, true);
            this.maxSize = maxSize;
        }

        /**
         * 強参照で保持するキャッシュサイズを設定する。現在保持している強参照のキャッシュレコードよりも
         * 小さい値を指定した場合は内部的に removeEldestEntry を呼び出して弱参照マップへ移行させる。
         * 
         * @param maxSize 強参照で保持するキャッシュサイズ
         */
        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
            if (size() > maxSize) {
                synchronized (_mutex) {
                    Iterator<Map.Entry<K, CountedReference<V>>> itr = entrySet().iterator();
                    while (itr.hasNext()) {
                        if (removeEldestEntry(itr.next())) {
                            itr.remove();
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        /**
         * @return the maxSize
         */
        public int getMaxSize() {
            return maxSize;
        }

        /**
         * LinkedHashMapの削除判定を行う。強参照で保持する件数を超える場合は、弱参照のマップへ移動させる。
         * 呼び出し元で synchronized ブロックで囲まれているため個別に動機化は必要ない。
         */
        @Override
        protected boolean removeEldestEntry(Map.Entry<K, CountedReference<V>> eldest) {
            if (size() > maxSize) {
                synchronized (_mutex) {
                    _weakReferenceMap.put(eldest.getKey(), eldest.getValue());
                    ++_droppedCount;
                    _maxCountOfDroppedRecord = Math.max(_maxCountOfDroppedRecord, eldest.getValue().getCount());
                }
                return true;
            }
            return false;
        }
    }

    public WeakValueHashMap() {
        this(DEFAULT_HARD_SIZE);
    }

    public WeakValueHashMap(int hardSize) {
        _hardReferenceLruMap = new LruHashMap(hardSize);
    }

    public V get(K key) {
        synchronized (_mutex) {
            CountedReference<V> ref = _hardReferenceLruMap.get(key);
            if (ref != null) {
                ref.countUp();
                return ref.get();
            }

            // assert(ref == null)
            ref = _weakReferenceMap.get(key);
            if (ref != null) {
                ref.countUp();
                ++_pulledUpCount;
                _hardReferenceLruMap.put(key, ref);
                _weakReferenceMap.remove(key);
                return ref.get();
            }

            return null;
        }
    }

    /**
     * putは比較的コストが高くても良いとする。
     * 
     * @param key
     * @param value
     */
    public void put(K key, V value) {
        synchronized (_mutex) {
            CountedReference<V> r = new CountedReference<V>(value);
            _hardReferenceLruMap.put(key, r);
            if (_weakReferenceMap.remove(key) != null) {
                // 弱参照のマップに含まれていた場合はPullUpされたことになる。
                // ただし値自体は変わっている可能性がある。
                ++_pulledUpCount;
            }
        }
    }

    public int size() {
        synchronized (_mutex) {
            return _weakReferenceMap.size() + _hardReferenceLruMap.size();
        }
    }

    public void setHardSize(int hardSize) {
        synchronized (_mutex) {
            _hardReferenceLruMap.setMaxSize(hardSize);
        }
    }

    public int getHardSize() {
        synchronized (_mutex) {
            return _hardReferenceLruMap.getMaxSize();
        }
    }

    public long getDroppedCount() {
        return _droppedCount;
    }

    public long getPullUpCount() {
        return _pulledUpCount;
    }

    public long getMaxCountOfDroppedRecord() {
        return _maxCountOfDroppedRecord;
    }
}

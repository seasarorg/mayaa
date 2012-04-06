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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * リファレンスキャッシュクラス。
 *
 * @author Taro Kato (Gluegent, Inc.)
 */
public class ReferenceCache extends ArrayList {

    private static final long serialVersionUID = -6808020476640514389L;

    public static final int SOFT = 0;
    public static final int WEAK = 1;

    /** 全スレッド共通の活動可能フラグ */
    static boolean _alive = true;

    /**
     * ReferenceCacheで活動している全スレッドを止める。
     */
    public static void finishThreads() {
        _alive = false;
    }

    /**
     * GC対象としてマークされ解放されるオブジェクトを
     * 通知する。オブジェクトは解放済みになるので、
     * あらかじめ対象オブジェクトに名前を付けて、
     * 解放時にはその名前で通知される。
     *
     * @author Taro Kato (Gluegent, Inc.)
     */
    public static interface SweepListener {

        /**
         * オブジェクトにラベルを付ける。
         * @param referent 解放監視対象オブジェクト
         * @return オブジェクトを識別するためのラベル。
         * referentと参照依存関係のあるオブジェクトを返してはならない。
         */
        Object labeling(Object referent);

        /**
         * labering時のreferentが解放された際に呼び出される。
         * laberingで返したlabelオブジェクトが渡される。
         * @param monitor リファレンスキャッシュ
         * @param label オブジェクトに対応付けていたラベル
         */
        void sweepFinish(ReferenceCache monitor, Object label);
    }

    protected volatile boolean _liveSweepMonitor;
    protected SweepListener _sweepBeginListener;
    protected ReferenceQueue _queue;

    private Class _elementType;
    private int _referenceType;
    private String _name;
    protected Map _labelReferenceMap;

    public ReferenceCache() {
        this(Object.class, SOFT, null);
    }

    public ReferenceCache(Class elementType) {
        this(elementType, SOFT, null);
    }

    public ReferenceCache(Class elementType, int referenceType) {
        this(elementType, SOFT, null);
    }

    public ReferenceCache(Class elementType,
            int referenceType, SweepListener listener) {
        if (referenceType != SOFT && referenceType != WEAK) {
            throw new IllegalArgumentException();
        }
        if (elementType == null) {
            throw new IllegalArgumentException();
        }
        _elementType = elementType;
        _referenceType = referenceType;
        _sweepBeginListener = listener;
    }

    private void check(Object element) {
        if (element == null
                || _elementType.isAssignableFrom(element.getClass()) == false) {
            throw new IllegalArgumentException();
        }
    }

    public void setName(String name) {
        _name = name;
    }

    public String getName() {
        return _name;
    }

    protected synchronized void sweepMonitorStart() {
        if (_liveSweepMonitor) {
            return;
        }
        ThreadGroup topThreadGroup;
        for (topThreadGroup = Thread.currentThread().getThreadGroup()
                ; topThreadGroup.getParent() != null
                ; topThreadGroup = topThreadGroup.getParent()) {
            /* no operation */
        }
        _labelReferenceMap = new HashMap();
        _queue = new ReferenceQueue();

        new Thread(topThreadGroup, "ReferenceCache Sweep Monitor") {
            {
                setPriority(Thread.MIN_PRIORITY);
                setDaemon(true);
                _liveSweepMonitor = true;
            }
            public void run() {
                while(_liveSweepMonitor && _alive) {
                     try {
                        PhantomReference ref =
                            (PhantomReference) _queue.remove(1);
                        if (ref != null) {
                            Object label = _labelReferenceMap.get(ref);
                            _labelReferenceMap.remove(ref);
                            _sweepBeginListener.sweepFinish(
                                    ReferenceCache.this, label);
                        }
                    } catch(InterruptedException e) {
                        // no operation
                    }
                }

            }
        }.start();
    }

    protected Reference createReference(Object referent) {
        if (_sweepBeginListener != null) {
            sweepMonitorStart();
            Object labelObject = _sweepBeginListener.labeling(referent);
            if (labelObject == null || labelObject == referent) {
                labelObject = referent.toString();
            }
            PhantomReference ref = new PhantomReference(referent, _queue);
            _labelReferenceMap.put(ref, labelObject);
        }
        switch(_referenceType) {
        case SOFT:
            return new SoftReference(referent);
        case WEAK:
            return new WeakReference(referent);
        }
        throw new IllegalStateException();
    }

    public void add(int index, Object element) {
        check(element);
        super.add(index, createReference(element));
    }

    public boolean add(Object o) {
        check(o);
        return super.add(createReference(o));
    }

    public Iterator iterator() {
        return new ReferenceCacheIterator(this);
    }

    protected void finalize() throws Throwable {
        _liveSweepMonitor = false;
        super.finalize();
    }

    // support class

    /**
     * 解放されてヌルになったアイテムをパックしながら有効な
     * アイテムを返すイテレータ
     * @author Taro Kato (Gluegent, Inc.)
     */
    protected static class ReferenceCacheIterator implements Iterator {

        private int _index;
        private Object _next;
        private List _list;

        public ReferenceCacheIterator(List list) {
            if (list == null) {
                throw new IllegalArgumentException();
            }
            _list = list;
            _index = list.size();
        }

        public boolean hasNext() {
            if (_next != null) {
                return true;
            }
            while (_next == null) {
                _index--;
                if (_index < 0) {
                    return false;
                }
                synchronized (_list) {
                    Reference ref = (Reference) _list.get(_index);
                    _next = ref.get();
                    if (_next == null) {
                        _list.remove(_index);
                    }
                }
            }
            return true;
        }

        public Object next() {
            if (_next == null && hasNext() == false) {
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

}

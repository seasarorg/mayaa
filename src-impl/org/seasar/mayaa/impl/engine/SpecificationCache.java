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
package org.seasar.mayaa.impl.engine;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.util.ReferenceCache;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SpecificationCache {

    static final Log LOG = LogFactory.getLog(SpecificationCache.class);

    /** 終了処理後かどうか。終了処理後は追加処理をしない。 */
    protected static volatile boolean _sweepThreadAlive = true;

    protected int _surviveLimit;
    //@GuardedBy(this)
    protected Map<String, ReferSpecification> _specifications = new HashMap<>();

    protected ReferenceCache<Object> _gcChecker;
    protected SoftReference<Object> _gabage;
    protected AtomicLong hitCount = new AtomicLong();
    protected AtomicLong missCount = new AtomicLong();

    public SpecificationCache(int surviveLimit) {
        _surviveLimit = surviveLimit;
        if (surviveLimit > 0 && !ParameterAware.IS_SECURE_WEB) {
            _gcChecker = new ReferenceCache<>(Object.class,
                    ReferenceCache.SOFT, new GCReceiver());
            postNewGabage();
        }
    }

    protected static void relaseThread() {
        _sweepThreadAlive = false;
    }

    protected void postNewGabage() {
        Object gabage = new Object();
        _gabage = new SoftReference<>(gabage);
        _gcChecker.add(gabage);
    }

    public boolean contains(String systemID) {
        synchronized(this) {
            return _specifications.containsKey(systemID);
        }
    }

    public int size() {
        return _specifications.size();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    private void countCacheHit(boolean hit) {
        if (hit) {
            if (hitCount.incrementAndGet() < 0) {
                // オーバーフローしたら正の値に補正
                hitCount.addAndGet(Long.MAX_VALUE);
                missCount.set(0);
            }    
        }
        else {
            if (missCount.incrementAndGet() < 0) {
                // オーバーフローしたら正の値に補正
                missCount.addAndGet(Long.MAX_VALUE);
                hitCount.set(0);
            }
        }
    }

    public Specification get(String systemID) {
        Specification spec = _get(systemID);
        countCacheHit(spec != null);
        return null;
    }

    private Specification _get(String systemID) {
        if (systemID == null) {
            throw new IllegalArgumentException();
        }
        ReferSpecification refer;
        synchronized(this) {
            refer = (ReferSpecification) _specifications.get(systemID);
        }
        if (refer == null) {
            return null;
        }
        Specification result = refer.getSpecification();
        if (refer.isDeprecated()) {
            return null;
        }
        return result;
    }

    public void add(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
            Specification old = _get(specification.getSystemID());
            if (old != null) {
                if (old == specification) {
                    return;
                }
            }
            ReferSpecification refer = new ReferSpecification(specification);
            _specifications.put(specification.getSystemID(), refer);
        }
    }

    public void release() {
        synchronized(this) {
            _specifications = null;
            relaseThread();
        }
    }

    // support class

    private class ReferSpecification {
        private Specification _specification;
        private volatile int _survivingCount;
        volatile boolean _deprecated;

        public ReferSpecification(Specification specification) {
            if (specification == null) {
                throw new IllegalArgumentException();
            }
            _specification = specification;
        }

        public Specification getSpecification() {
            // 参照されたのでリセット
            _survivingCount = 0;
            return _specification;
        }

        public boolean isDeprecated() {
            if (_deprecated == false) {
                if (_specification.isDeprecated()) {
                    _deprecated = true;
                }
            }
            return _deprecated;
        }

        public boolean requestRelease() {
            _survivingCount++;
            if (_survivingCount > _surviveLimit) {
                _survivingCount = 0;
                _deprecated = true;
                return true;
            }
            return false;
        }
    }

    private class GCReceiver implements ReferenceCache.SweepListener {
        private volatile int _receiveCount = 0;

        protected GCReceiver() {
            // do nothing.
        }

        public Object labeling(Object referent) {
            return Integer.valueOf(++_receiveCount);
        }

        public void sweepFinish(ReferenceCache<?> monitor, Object label) {
            synchronized (SpecificationCache.this) {
                if (_specifications == null ||_sweepThreadAlive == false) {
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("remove " + label +"th time."
                            + " free:" + Runtime.getRuntime().freeMemory()
                            + " / total:" + Runtime.getRuntime().totalMemory());
                }
                List<ReferSpecification> releaseItems = null;
                for (ReferSpecification refer : _specifications.values()) {
                    if (refer.requestRelease()) {
                        if (releaseItems == null) {
                            releaseItems = new ArrayList<>();
                        }
                        releaseItems.add(refer);
                    }
                }
                if (releaseItems != null) {
                    for (ReferSpecification refer : releaseItems) {
                        Specification spec = refer.getSpecification();
                        _specifications.remove(spec.getSystemID());

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("remove " + label +"th time. "
                                    + spec.getSystemID() + " remove from cache");
                        }
                    }
                }
                postNewGabage(); /*gabage polling next*/
            }
        }
    }

}


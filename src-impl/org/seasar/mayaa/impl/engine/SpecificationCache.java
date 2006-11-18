/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.MayaaContext;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.util.ReferenceCache;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SpecificationCache {
    protected int _surviveLimit;
    protected Map _specifications = new HashMap();

    protected ReferenceCache _gcChecker;
    protected static TimeredSweepThread _cleanUpSpecification =
        new TimeredSweepThread();
    protected SoftReference _gabage;

    public SpecificationCache(int surviveLimit) {
        _surviveLimit = surviveLimit;
        if (surviveLimit > 0) {
            Object contextKey = MayaaContext.getCurrentContext().getApplicationContext();
            _gcChecker = new ReferenceCache(Object.class,
                    ReferenceCache.SOFT, new GCReceiver(contextKey));
            postNewGabage();
        }
    }

    protected void postNewGabage() {
        Object gabage = new Object();
        _gabage = new SoftReference(gabage);
        _gcChecker.add(gabage);
    }

    public boolean contains(String systemID) {
        synchronized(this) {
            return _specifications.containsKey(systemID);
        }
    }

    public Specification get(String systemID) {
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
            _cleanUpSpecification.post(MayaaContext.getCurrentContext(), result);
            return null;
        }
        return result;
    }

    public void add(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        synchronized(this) {
            Specification old = get(specification.getSystemID());
            if (old != null) {
                if (old == specification) {
                    return;
                }
                // ファイルシステムの違いによって大文字小文字が区別されない場合に
                // ここに到達する場合があるので。
                _cleanUpSpecification.post(MayaaContext.getCurrentContext(), old);
            }
            ReferSpecification refer =
                new ReferSpecification(specification);
            _specifications.put(specification.getSystemID(), refer);
        }
    }

    public void release() {
        synchronized(this) {
            _specifications = null;
            _cleanUpSpecification = null;
        }
    }

    // support class

    private class ReferSpecification {
        private Specification _specification;
        private int _survivingCount;
        boolean _deprecated;

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

    private static class TimeredSweepThread extends Thread {

        private Map _releaseSpecs = new LinkedHashMap();

        public TimeredSweepThread() {
            setName("TimeredSweepThread");
            start();
        }

        public void post(final MayaaContext targetContext,
                final Specification releaseSpec) {
            new Thread(new Runnable() {
                public void run() {
                    add(targetContext, releaseSpec);
                }
            }).start();
        }

        protected void add(final MayaaContext targetContext,
                final Specification releaseSpec) {
            long timing = System.currentTimeMillis() + (30 * 1000); // 30 sec wait
            synchronized(_releaseSpecs) {
                ReleaseTargetHolder holder =
                    (ReleaseTargetHolder) _releaseSpecs.get(releaseSpec);
                if (holder == null) {
                    _releaseSpecs.put(
                            releaseSpec,
                            new ReleaseTargetHolder(targetContext, releaseSpec, timing));
                } else {
                    holder.setTimeup(timing);
                }
            }
        }

        public void run() {
            while (_cleanUpSpecification != null) {
                try {
                    Thread.sleep(1000);
                    synchronized(_releaseSpecs) {
                        for (Iterator it = _releaseSpecs.entrySet().iterator()
                                ; it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry) it.next();
                            ReleaseTargetHolder target =
                                (ReleaseTargetHolder) entry.getValue();
                            if (System.currentTimeMillis() > target.getTimeup()) {
                                target.kill();
                                it.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    // no operation
                }
            }
        }
    }

    /**
     * リリース対象のコンテキストとSpecificationをセットで保持する。
     */
    private static class ReleaseTargetHolder {
        private MayaaContext _targetContext;
        private Specification _releaseSpec;
        private long _timeup;

        protected ReleaseTargetHolder(
                final MayaaContext targetContext,
                final Specification releaseSpec, final long timeup) {
            _targetContext = targetContext;
            _releaseSpec = releaseSpec;
            _timeup = timeup;
        }

        protected void kill() {
            if (_releaseSpec != null) {
                MayaaContext.setCurrentContext(_targetContext);
                _releaseSpec.kill();
                _releaseSpec = null;
                _targetContext = null;
            }
        }

        protected long getTimeup() {
            return _timeup;
        }

        protected void setTimeup(long timeup) {
            _timeup = timeup;
        }

    }

    private class GCReceiver implements ReferenceCache.SweepListener {
        private final Log LOG = LogFactory.getLog(SpecificationCache.class);
        private volatile int _receiveCount = 0;
        private Object _contextKey;

        protected GCReceiver(Object contextKey) {
            _contextKey = contextKey;
        }

        public Object labeling(Object referent) {
            return new Integer(++_receiveCount);
        }

        public void sweepFinish(ReferenceCache monitor, Object label) {
            synchronized(SpecificationCache.this) {
                if (_specifications == null || _cleanUpSpecification == null) {
                    return;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("remove " + label +"th time."
                            + " free:" + Runtime.getRuntime().freeMemory()
                            + " / total:" + Runtime.getRuntime().totalMemory());
                }
                List releaseItems = null;
                for (Iterator it = _specifications.values().iterator()
                        ; it.hasNext(); ) {
                    ReferSpecification refer = (ReferSpecification) it.next();
                    if (refer.requestRelease()) {
                        if (releaseItems == null) {
                            releaseItems = new ArrayList();
                        }
                        releaseItems.add(refer);
                    }
                }
                if (releaseItems != null) {
                    MayaaContext context = MayaaContext.getContext(_contextKey);
                    for (Iterator it = releaseItems.iterator()
                            ; it.hasNext(); ) {
                        ReferSpecification refer =
                            (ReferSpecification) it.next();
                        Specification spec = refer.getSpecification();
                        _cleanUpSpecification.add(context, spec);
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


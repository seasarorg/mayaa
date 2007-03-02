/*
 * Copyright 2004-2007 the Seasar Foundation and the Others.
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
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.util.ReferenceCache;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SpecificationCache {

    static final Log LOG = LogFactory.getLog(SpecificationCache.class);

    protected static volatile TimeredSweepThread _cleanUpSpecification =
        new TimeredSweepThread();
    /** 終了処理後かどうか。終了処理後は追加処理をしない。 */
    protected static volatile boolean _sweepThreadAlive = true;

    protected int _surviveLimit;
    //@GuardedBy(this)
    protected Map _specifications = new HashMap();

    protected ReferenceCache _gcChecker;
    protected SoftReference _gabage;

    public SpecificationCache(int surviveLimit) {
        _surviveLimit = surviveLimit;
        if (surviveLimit > 0) {
            _gcChecker = new ReferenceCache(Object.class,
                    ReferenceCache.SOFT, new GCReceiver());
            postNewGabage();
        }
    }

    protected static void relaseThread() {
        _sweepThreadAlive = false;
        _cleanUpSpecification = null;
    }

    protected void postNewGabage() {
        Object gabage = new Object();
        _gabage = new SoftReference(gabage);
        _gcChecker.add(gabage);
    }

    /**
     * 掃除スレッドに投げます。
     * 呼ぶ側でSpecificationCache.thisでsynchronizedして呼ぶようにします。
     *
     * @param releaseSpec
     */
    protected void postGabage(final Specification releaseSpec) {
        if (_sweepThreadAlive) {
            _cleanUpSpecification.post(releaseSpec);
        }
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
            synchronized(this) {
                postGabage(result);
            }
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
                postGabage(old);
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

    /**
     * Specificationインスタンスの解放処理を独自スレッドで実行します。
     * コンストラクタでデーモンスレッドとして動作を開始します。
     * 解放したいSpecificationはpostメソッドで渡します。
     *
     * @author Taro Kato (Gluegent Inc.)
     */
    private static class TimeredSweepThread extends Thread {

        private static long DELAY = 30000; // 30 sec wait
        private Map _repeaseSpecs = new LinkedHashMap();

        public TimeredSweepThread() {
            setName("TimeredSweepThread");
            setDaemon(true);
            start();
        }

        public void post(final Specification releaseSpec) {
            new Thread(new Runnable() {
                public void run() {
                    add(releaseSpec);
                }
            }).start();
        }

        protected void add(Specification releaseSpec) {
            long timing = System.currentTimeMillis() + DELAY;
            synchronized(_repeaseSpecs) {
                _repeaseSpecs.put(releaseSpec, new Long(timing));
            }
        }

        public void run() {
            while (_sweepThreadAlive) {
                try {
                    Thread.sleep(1000);
                    List killedspec = new ArrayList();
                    synchronized(_repeaseSpecs) {
                        for (Iterator it = _repeaseSpecs.entrySet().iterator()
                                ; it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry) it.next();
                            long timeup = ((Long) entry.getValue()).longValue();
                            if (System.currentTimeMillis() > timeup) {
                                // まだ参照されているなら猶予する
                                long renderingCount =
                                    RenderUtil.getRenderingCount(entry.getKey());
                                if (renderingCount > 0) {
                                    entry.setValue(new Long(timeup + DELAY));
                                    if (LOG.isTraceEnabled()) {
                                        LOG.trace("delaying GC because rendering now: "
                                                + entry.getKey() + ", " + renderingCount);
                                    }
                                } else {
                                    killedspec.add(entry.getKey());
                                    it.remove();
                                }
                            }
                        }
                    }
                    for (Iterator it = killedspec.iterator(); it.hasNext();) {
                        Specification spec = (Specification) it.next();
                        spec.kill();
                    }
                } catch (InterruptedException e) {
                    // no operation
                }
            }
        }
    }

    private class GCReceiver implements ReferenceCache.SweepListener {
        private volatile int _receiveCount = 0;

        protected GCReceiver() {
            // do nothing.
        }

        public Object labeling(Object referent) {
            return new Integer(++_receiveCount);
        }

        public void sweepFinish(ReferenceCache monitor, Object label) {
            synchronized (SpecificationCache.this) {
                if (_specifications == null ||_sweepThreadAlive == false) {
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
                    for (Iterator it = releaseItems.iterator()
                            ; it.hasNext(); ) {
                        ReferSpecification refer =
                            (ReferSpecification) it.next();
                        Specification spec = refer.getSpecification();
                        _specifications.remove(spec.getSystemID());
                        _cleanUpSpecification.add(spec);
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


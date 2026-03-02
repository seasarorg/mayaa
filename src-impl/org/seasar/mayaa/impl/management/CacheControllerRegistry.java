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
package org.seasar.mayaa.impl.management;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.seasar.mayaa.management.CacheControlMXBean;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Policy;

/**
 * 
 * @since 1.2
 * @author Watanabe, Mitsutaka
 */
public class CacheControllerRegistry {
    public static void registerCacheController(String controllerName, final Cache<?,?> cache) {
        CacheControlMXBean mbean = new CacheControlMXBean(){
            private Policy.Eviction<?, ?> getEvictionPolicy() {
                return cache.policy().eviction().orElse(null);
            }

            @Override
            public String getClassName() {
                return cache.getClass().getName();
            }
            @Override
            public int getRetainSize() {
                Policy.Eviction<?, ?> eviction = getEvictionPolicy();
                if (eviction == null) {
                    return -1;
                }
                long maximum = eviction.getMaximum();
                return maximum > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maximum;
            }
            @Override
            public void setRetainSize(int retainSize) {
                if (retainSize < 0) {
                    throw new IllegalArgumentException("retainSize must be >= 0");
                }
                Policy.Eviction<?, ?> eviction = getEvictionPolicy();
                if (eviction == null) {
                    throw new UnsupportedOperationException("Managing retain size is not supported");
                }
                eviction.setMaximum(retainSize);
            }
            @Override
            public long getCurrentSize() {
                return cache.estimatedSize();
            }

            @Override
            public long getRequestCount() {
                return cache.stats().requestCount();
            }

            @Override
            public long getHitCount() {
                return cache.stats().hitCount();
            }

            @Override
            public double getHitRate() {
                return cache.stats().hitRate();
            }

            @Override
            public long getMissCount() {
                return cache.stats().missCount();
            }

            @Override
            public double getMissRate() {
                return cache.stats().missRate();
            }

            @Override
            public long getLoadSuccessCount() {
                return cache.stats().loadSuccessCount();
            }

            @Override
            public long getLoadFailureCount() {
                return cache.stats().loadFailureCount();
            }

            @Override
            public long getTotalLoadTime() {
                return cache.stats().totalLoadTime();
            }

            @Override
            public long getEvictionCount() {
                return cache.stats().evictionCount();
            }

            @Override
            public boolean isStatsEnabled() {
                return true;
            }

            @Override
            public boolean isMaximumSizeManageable() {
                return getEvictionPolicy() != null;
            }

            @Override
            public void invalidateAll() {
                cache.invalidateAll();
            }
        };
        JMXUtil.register(mbean, makeObjectName(controllerName));
    }

    protected static ObjectName makeObjectName(String controllerName) {
        try {
            String name = String.format(CacheControlMXBean.JMX_OBJECT_NAME_FORMAT, controllerName);
            ObjectName objectName = new ObjectName(name);
            return objectName;
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("ObjectName is invalid");
        }
    }

}
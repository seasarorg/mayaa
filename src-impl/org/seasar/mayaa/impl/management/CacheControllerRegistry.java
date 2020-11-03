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

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.impl.engine.SpecificationCache;
import org.seasar.mayaa.impl.util.WeakValueHashMap;
import org.seasar.mayaa.management.CacheControlMXBean;

/**
 * 
 * @since 1.2
 * @author Watanabe, Mitsutaka
 */
public class CacheControllerRegistry {

    public static void registerCacheController(String controllerName, final Map<?,?> map) {
        CacheControlMXBean mbean = new CacheControlMXBean(){
            @Override
            public String getClassName() {
                return map.getClass().getName();
            }
            @Override
            public int getRetainSize() {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public void setRetainSize(int retainSize) {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public long getCurrentSize() {
                return map.size();
            }
            @Override
            public long getHitCount() {
                throw new UnsupportedOperationException("Collecting cache hit count is not supported");
            }

            @Override
            public long getMissCount() {
                throw new UnsupportedOperationException("Collecting cache miss count is not supported");
            }
        };
        JMXUtil.register(mbean, makeObjectName(controllerName));
    }

    public static void registerCacheController(String controllerName, final SpecificationCache specificationCache) {
        CacheControlMXBean mbean = new CacheControlMXBean(){
            @Override
            public String getClassName() {
                return specificationCache.getClass().getName();
            }
            @Override
            public int getRetainSize() {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public void setRetainSize(int retainSize) {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public long getCurrentSize() {
                return specificationCache.size();
            }

            @Override
            public long getHitCount() {
                return specificationCache.getHitCount();
            }

            @Override
            public long getMissCount() {
                return specificationCache.getMissCount();
            }
        };
        JMXUtil.register(mbean, makeObjectName(controllerName));
    }

    public static void registerCacheController(String controllerName, final WeakValueHashMap<?,?> weakValueHashMap) {
        CacheControlMXBean mbean = new CacheControlMXBean(){
            @Override
            public String getClassName() {
                return weakValueHashMap.getClass().getName();
            }
            @Override
            public int getRetainSize() {
                return weakValueHashMap.getHardSize();
            }
            @Override
            public void setRetainSize(int retainSize) {
                weakValueHashMap.setHardSize(retainSize);
            }
            @Override
            public long getCurrentSize() {
                return weakValueHashMap.size();
            }

            @Override
            public long getHitCount() {
                return weakValueHashMap.getHitCount();
            }

            @Override
            public long getMissCount() {
                return weakValueHashMap.getMissCount();
            }
        };
        JMXUtil.register(mbean, makeObjectName(controllerName));
    }

    public static void registerCacheController(String controllerName, final ReferenceMap referenceMap) {
        CacheControlMXBean mbean = new CacheControlMXBean(){
            @Override
            public String getClassName() {
                return referenceMap.getClass().getName();
            }
            @Override
            public int getRetainSize() {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public void setRetainSize(int retainSize) {
                throw new UnsupportedOperationException("Managing retain size is not supported");
            }
            @Override
            public long getCurrentSize() {
                return referenceMap.size();
            }
            @Override
            public long getHitCount() {
                throw new UnsupportedOperationException("Collecting cache hit count is not supported");
            }

            @Override
            public long getMissCount() {
                throw new UnsupportedOperationException("Collecting cache miss count is not supported");
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
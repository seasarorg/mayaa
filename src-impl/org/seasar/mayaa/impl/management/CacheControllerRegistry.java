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

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.impl.engine.SpecificationCache;
import org.seasar.mayaa.impl.util.WeakValueHashMap;
import org.seasar.mayaa.management.CacheControlMBean;

/**
 * 
 * @since 1.1.35
 * @author Watanabe, Mitsutaka
 */
public class CacheControllerRegistry {

    public static void registerCacheController(String controllerName, final Map<?,?> map) {
        CacheControlMBean mbean = new CacheControlMBean(){
            @Override
            public String getClassName() {
                return map.getClass().getName();
            }
            @Override
            public long getMaxSize() {
                throw new UnsupportedOperationException("Managing max cache size is not supported");
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
        register(controllerName, mbean);
    }

    public static void registerCacheController(String controllerName, final SpecificationCache specificationCache) {
        CacheControlMBean mbean = new CacheControlMBean(){
            @Override
            public String getClassName() {
                return specificationCache.getClass().getName();
            }
            @Override
            public long getMaxSize() {
                throw new UnsupportedOperationException("Managing max cache size is not supported");
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
        register(controllerName, mbean);
    }

    public static void registerCacheController(String controllerName, final WeakValueHashMap<?,?> weakValueHashMap) {
        CacheControlMBean mbean = new CacheControlMBean(){
            @Override
            public String getClassName() {
                return weakValueHashMap.getClass().getName();
            }
            @Override
            public long getMaxSize() {
                return weakValueHashMap.getHardSize();
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
        register(controllerName, mbean);
    }

    public static void registerCacheController(String controllerName, final ReferenceMap referenceMap) {
        CacheControlMBean mbean = new CacheControlMBean(){
            @Override
            public String getClassName() {
                return referenceMap.getClass().getName();
            }
            @Override
            public long getMaxSize() {
                throw new UnsupportedOperationException("Managing max cache size is not supported");
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
        register(controllerName, mbean);
    }

    protected static void register(String controllerName, CacheControlMBean cacheControlMBean) {
        if (controllerName == null || controllerName.isEmpty() || cacheControlMBean == null) {
            throw new IllegalArgumentException();
        }
        ObjectName objectName;
        try {
            String name = String.format(CacheControlMBean.JMX_OBJECT_NAME_FORMAT, controllerName);
            objectName = new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("ObjectName is invalid");
        }

        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer(); // MBeanServer を取得

            // 重複登録されるとInstanceAlreadyExistsExceptionが発生するので事前に削除しておく。
            try {
                mBeanServer.unregisterMBean(objectName);
            } catch (InstanceNotFoundException e) {
                // Ignore
            }
            mBeanServer.registerMBean(new StandardMBean(cacheControlMBean, CacheControlMBean.class), objectName);
        } catch (MBeanRegistrationException | NotCompliantMBeanException e) {
                    System.err.println(e);
        } catch (InstanceAlreadyExistsException e) {
        }
    }

}
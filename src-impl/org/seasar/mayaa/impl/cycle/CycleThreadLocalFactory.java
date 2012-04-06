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
package org.seasar.mayaa.impl.cycle;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.cycle.CycleLocalInstantiator;

/**
 * リクエストのライフサイクルだけ有効なオブジェクトを管理する。
 * @author Taro Kato (Gluegent, Inc.)
 */
public class CycleThreadLocalFactory {

    private static ThreadLocal/*<Map>*/ _cycleLocalVariables = new ThreadLocal();
    private static Map/*<String, Instantiator>*/ _instantiators = new HashMap();

    private CycleThreadLocalFactory() {
        throw new UnsupportedOperationException();
    }

    protected static void cycleLocalInitialize() {
        _cycleLocalVariables.set(new HashMap());
    }

    protected static Map getThreadLocalMap() {
        Map map = (Map) _cycleLocalVariables.get();
        if (map == null) {
            cycleLocalInitialize();
            map = (Map) _cycleLocalVariables.get();
        }
        return map;
    }

    public static void cycleLocalFinalize() {
        Map map = (Map) _cycleLocalVariables.get();
        if (map != null) {
            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                Object value = entry.getValue();
                Object key = entry.getKey();
                if (key instanceof InstanceKey) {
                    key = ((InstanceKey)key).getKey();
                }
                CycleLocalInstantiator instantiator =
                    (CycleLocalInstantiator) _instantiators.get(key);
                instantiator.destroy(value);
            }
            map.clear();
        }
        _cycleLocalVariables.set(null);
    }

    public static void registFactory(String key, CycleLocalInstantiator instantiator) {
        _instantiators.put(key, instantiator);
    }

    public static void clearLocalVariable(Object key) {
        Map map = getThreadLocalMap();
        Object instance = map.get(key);
        if (instance != null) {
            String strKey;
            if (key instanceof InstanceKey) {
                strKey = ((InstanceKey) key).getKey();
            } else {
                strKey = key.toString();
            }
            CycleLocalInstantiator instantiator =
                (CycleLocalInstantiator) _instantiators.get(strKey);
            if (instantiator != null) {
                instantiator.destroy(instance);
            }
            map.put(key, null);
        }
    }

    public static Object get(Object key, Object[] params) {
        Map localVariables = getThreadLocalMap();
        Object result = localVariables.get(key);
        if (result == null) {
            String strKey;
            Object owner;
            if (key instanceof InstanceKey) {
                strKey = ((InstanceKey) key).getKey();
                owner = ((InstanceKey) key).getOwner();
            } else {
                strKey = key.toString();
                owner = null;
            }
            CycleLocalInstantiator instantiator = (CycleLocalInstantiator)_instantiators.get(strKey);
            if (instantiator == null) {
                throw new IllegalStateException("inistantiator unknown. key = " + key);
            }
            if (owner == null) {
                result = instantiator.create(params);
            } else {
                result = instantiator.create(owner, params);
            }
            localVariables.put(key, result);
        }
        return result;
    }

    public static void set(Object key, Object value) {
        Map localVariables = getThreadLocalMap();
        Object existValue = localVariables.get(key);
        if (existValue != null) {
            clearLocalVariable(key);
        }
        localVariables.put(key, value);
    }

    public static class InstanceKey {
        private String _key;
        private Object _owner;

        public InstanceKey(String key, Object owner) {
            _key = key;
            _owner = owner;
        }

        public String getKey() {
            return _key;
        }

        public Object getOwner() {
            return _owner;
        }

        public int hashCode() {
            return _key.hashCode() + _owner.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof InstanceKey) {
                InstanceKey other = (InstanceKey) obj;
                return (other._owner == _owner
                        && other._key.equals(_key));
            }
            return false;
        }
    }
}

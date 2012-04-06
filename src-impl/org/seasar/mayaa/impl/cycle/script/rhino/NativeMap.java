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
package org.seasar.mayaa.impl.cycle.script.rhino;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NativeMap extends NativeJavaObject {

    private static final long serialVersionUID = -3987211835989098780L;

    private Map _map;

    public NativeMap(Scriptable scope, Map map) {
        super(scope, map, Map.class);
        if (map == null) {
            throw new IllegalArgumentException();
        }
        _map = map;
    }

    public boolean has(String name, Scriptable start) {
        if (_map.containsKey(name)) {
            return true;
        }
        return super.has(name, start);
    }

    public Object get(String name, Scriptable start) {
        if (_map.containsKey(name)) {
            return _map.get(name);
        }
        return super.get(name, start);
    }

    public void put(String name, Scriptable start, Object value) {
        _map.put(name, value);
    }

    public Object[] getIds() {
        Set set = new HashSet(_map.keySet());
        Object[] ids = super.getIds();
        for (int i = 0; i < ids.length; i++) {
            Object name = ids[i];
            if (set.contains(name) == false) {
                set.add(name);
            }
        }
        return set.toArray(new Object[set.size()]);
    }

    public String getClassName() {
        return "javaMap";
    }

}

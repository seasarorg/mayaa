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
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

/**
 * DynaBeanに対してRhinoスクリプト中で、通常JSで用いられる記法
 * によるプロパティアクセスするためのドライバ。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class NativeDynaBean extends NativeJavaObject {

    private static final long serialVersionUID = -8488937538584671606L;

    private DynaBean _bean;

    public NativeDynaBean(Scriptable script, DynaBean bean) {
        super(script, bean, DynaBean.class);
        if(bean == null) {
            throw new IllegalArgumentException();
        }
        _bean = bean;
    }

    public boolean has(String name, Scriptable start) {
        DynaClass clazz = _bean.getDynaClass();
        DynaProperty prop = clazz.getDynaProperty(name);
        return prop != null ? true : super.has(name, start);
    }

    /**
     * nameがDynaBeanのメソッドでないかどうかを返します。
     *
     * @param name 調べるプロパティ名
     * @return DynaBeanのメソッドでないならtrue
     */
    protected boolean isNotDynaBeanMethod(String name) {
        return name.equals("get") == false && name.equals("set") == false &&
                name.equals("remove") == false && name.equals("contains") == false &&
                name.equals("dynaClass") == false && name.equals("getDynaClass") == false;
    }

    public Object get(String name, Scriptable start) {
        if (isNotDynaBeanMethod(name) && has(name, start)) {
            return _bean.get(name);
        }
        return super.get(name, start);
    }

    public void put(String name, Scriptable start, Object value) {
        if (isNotDynaBeanMethod(name) && has(name, start)) {
            _bean.set(name, value);
        } else {
            super.put(name, start, value);
        }
    }

    public Object[] getIds() {
        DynaClass clazz = _bean.getDynaClass();
        DynaProperty[] props = clazz.getDynaProperties();
        Set set = new HashSet();
        for (int i = 0; i < props.length; i++) {
            set.add(props[i].getName());
        }
        Object[] ids = super.getIds();
        for (int i = 0; i < ids.length; i++) {
            if(set.contains(ids[i]) == false) {
                set.add(ids[i]);
            }
        }
        return set.toArray();
    }

    public String getClassName() {
        return "DynaBean";
    }

}

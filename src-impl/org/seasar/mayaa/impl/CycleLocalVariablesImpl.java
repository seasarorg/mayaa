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
package org.seasar.mayaa.impl;

import java.io.Serializable;

import org.seasar.mayaa.cycle.CycleLocalVariables;
import org.seasar.mayaa.impl.cycle.CycleThreadLocalFactory;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class CycleLocalVariablesImpl implements CycleLocalVariables, Serializable {

    private static final long serialVersionUID = 981360496015071116L;

    public Object getGlobalVariable(String key, Object[] params) {
        return CycleThreadLocalFactory.get(key, params);
    }

    public void setGlobalVariable(String key, Object value) {
        CycleThreadLocalFactory.set(key, value);
    }

    public void clearGlobalVariable(String key) {
        CycleThreadLocalFactory.clearLocalVariable(key);

    }

    public void clearVariable(String key, Object value) {
        CycleThreadLocalFactory.clearLocalVariable(
                new CycleThreadLocalFactory.InstanceKey(key, value));
    }

    public Object getVariable(String key, Object owner, Object[] params) {
        return CycleThreadLocalFactory.get(
                new CycleThreadLocalFactory.InstanceKey(key, owner), params);
    }

    public void setVariable(String key, Object owner, Object value) {
        CycleThreadLocalFactory.set(
                new CycleThreadLocalFactory.InstanceKey(key, owner), value);
    }

}


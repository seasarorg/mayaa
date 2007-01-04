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
package org.seasar.mayaa.impl.cycle;

import org.seasar.mayaa.cycle.CycleLocalInstantiator;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class DefaultCycleLocalInstantiator implements CycleLocalInstantiator {

    public Object create(Object[] params) {
        throw new UnsupportedOperationException();
    }

    public Object create(Object owner, Object[] params) {
        throw new UnsupportedOperationException();
    }

    public void destroy(Object instance) {
        // no-op
    }

}


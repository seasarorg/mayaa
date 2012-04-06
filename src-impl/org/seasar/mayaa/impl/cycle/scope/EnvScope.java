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
package org.seasar.mayaa.impl.cycle.scope;

import java.util.Iterator;

/**
 * 環境変数を取得する読み込み専用スコープ。
 *
 * @see java.lang.System#getProperty(String)
 * @author Koji Suga (Gluegent, Inc)
 */
public class EnvScope extends AbstractReadOnlyAttributeScope {

    private static final long serialVersionUID = -7066469372221202023L;

    // AttributeScope implements -------------------------------------

    public String getScopeName() {
        return "env";
    }

    public Iterator iterateAttributeNames() {
        return System.getProperties().keySet().iterator();
    }

    public boolean hasAttribute(String name) {
        return System.getProperty(name) != null;
    }

    //TODO キーはあって値が無い場合の対応を検討（AttributeScope全般）。
    public Object getAttribute(String name) {
        if (hasAttribute(name)) {
            return System.getProperty(name);
        }
        return null;
    }

}

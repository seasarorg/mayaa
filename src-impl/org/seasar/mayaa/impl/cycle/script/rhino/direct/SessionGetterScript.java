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
package org.seasar.mayaa.impl.cycle.script.rhino.direct;

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.cycle.scope.AttributeScope;
import org.seasar.mayaa.cycle.scope.SessionScope;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * sessionスコープから変数を取得するだけの処理をするスクリプト。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class SessionGetterScript extends AbstractGetterScript {

    private static final long serialVersionUID = 1L;

    private static final String[] PROPERTY_NAMES =
        ObjectUtil.getPropertyNames(SessionScope.class);

    public SessionGetterScript(
            String text, PositionAware position, int offsetLine,
            String scopeName, String attributeName, String propertyName) {
        super(text, position, offsetLine, scopeName, attributeName, propertyName, PROPERTY_NAMES);
    }

    /**
     * sessionスコープを返します。
     *
     * @return sessionスコープ。
     */
    protected AttributeScope getScope() {
        return CycleUtil.getServiceCycle().getSessionScope();
    }

}

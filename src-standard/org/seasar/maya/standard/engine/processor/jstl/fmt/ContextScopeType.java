/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.standard.engine.processor.jstl.fmt;

import javax.servlet.jsp.PageContext;

/**
 * Scopeの種類.
 * @author suga
 */
public class ContextScopeType {
    
    public static final ContextScopeType APPLICATION =
        new ContextScopeType(PageContext.APPLICATION_SCOPE, "application");
    public static final ContextScopeType SESSION =
        new ContextScopeType(PageContext.SESSION_SCOPE, "session");
    public static final ContextScopeType REQUEST =
        new ContextScopeType(PageContext.REQUEST_SCOPE, "request");
    public static final ContextScopeType PAGE =
        new ContextScopeType(PageContext.PAGE_SCOPE, "page");

    private int _value;
    private String _name;

    private ContextScopeType() {
    }

    private ContextScopeType(int value, String name) {
        _value = value;
        _name = name;
    }

    /**
     * スコープの値を取得する。
     *
     * @return スコープの値
     */
    public int getValue() {
        return _value;
    }

    /**
     * スコープ名を取得する。
     *
     * @return スコープ名
     */
    public String getName() {
        return _name;
    }

    /**
     * 文字列からScopeTypeを返す。
     * 未定義値の場合、NESTEDを返す。
     *
     * @param name スコープ名
     * @return ContextScopeType
     */
    public static ContextScopeType getByName(String name) {
        if (REQUEST._name.equalsIgnoreCase(name)) {
            return REQUEST;
        } else if (SESSION._name.equalsIgnoreCase(name)) {
            return SESSION;
        } else if (APPLICATION._name.equalsIgnoreCase(name)) {
            return APPLICATION;
        } else {
            return PAGE;
        }
    }

    /**
     * 値からScopeTypeを返す。
     * 未定義値の場合、NESTEDを返す。
     *
     * @param value スコープ値
     * @return ContextScopeType
     */
    public static ContextScopeType getByValue(int value) {
        if (REQUEST._value == value) {
            return REQUEST;
        } else if (SESSION._value == value) {
            return SESSION;
        } else if (APPLICATION._value == value) {
            return APPLICATION;
        } else {
            return PAGE;
        }
    }
    
}

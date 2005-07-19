/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.standard.builder.library;

import javax.servlet.jsp.tagext.VariableInfo;

/**
 * Scopeの種類.
 * @author suga
 */
public class ScopeType {
    
    public static final ScopeType AT_BEGIN = new ScopeType(VariableInfo.AT_BEGIN, "AT_BEGIN");
    public static final ScopeType AT_END = new ScopeType(VariableInfo.AT_END, "AT_END");
    public static final ScopeType NESTED = new ScopeType(VariableInfo.NESTED, "NESTED");

    private int _value;
    private String _name;

    private ScopeType() {
    }

    private ScopeType(int value, String name) {
        _value = value;
        _name = name;
    }

    public int getValue() {
        return _value;
    }

    public String getName() {
        return _name;
    }

    /**
     * 文字列からScopeTypeを返す。
     * 未定義値の場合、NESTEDを返す。
     * @param name スコープ名
     * @return ScopeType
     */
    public static ScopeType getByName(String name) {
        if (AT_BEGIN._name.equalsIgnoreCase(name)) {
            return AT_BEGIN;
        } else if (AT_END._name.equalsIgnoreCase(name)) {
            return AT_END;
        } else {
            return NESTED;
        }
    }

    /**
     * 値からScopeTypeを返す。
     * 未定義値の場合、NESTEDを返す。
     * @param value スコープ値
     * @return ScopeType
     */
    public static ScopeType getByValue(int value) {
        if (AT_BEGIN._value == value) {
            return AT_BEGIN;
        } else if (AT_END._value == value) {
            return AT_END;
        } else {
            return NESTED;
        }
    }

}

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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.seasar.mayaa.impl.cycle.script.JsonScriptSerializer;
import org.seasar.mayaa.impl.util.EscapeUtil;

/**
 * Script 内の Scope マクロを支える Rhino グローバル関数群。
 *
 * _mayaa_scope は評価結果を JavaScript 式として妥当なソース断片へ変換し、
 * _mayaa_scope_as_string は評価結果を常に JavaScript 文字列リテラルへ変換し、
 * _mayaa_scope_with_stringify は評価結果を JSON.stringify 相当のテキストへ変換する。
 */
public final class ScopeMacroFunctionSupport {

    private ScopeMacroFunctionSupport() {
        // no instantiation
    }

    public static Object _mayaa_scope(
            Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Object value = firstArg(args);
        if (value == null || value == Undefined.instance) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return Context.toString(value);
        }
        return quoteJavaScriptStringLiteral(Context.toString(value));
    }

    public static Object _mayaa_scope_as_string(
            Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        Object value = firstArg(args);
        if (value == null || value == Undefined.instance) {
            return quoteJavaScriptStringLiteral("");
        }
        return quoteJavaScriptStringLiteral(Context.toString(value));
    }

    public static Object _mayaa_scope_with_stringify(
            Context cx, Scriptable thisObj, Object[] args, Function funObj) {
        return JsonScriptSerializer.serialize(toSerializableValue(firstArg(args)));
    }

    private static Object toSerializableValue(Object value) {
        if (value == null || value == Undefined.instance) {
            return null;
        }
        if (value instanceof Wrapper) {
            Object unwrapped = ((Wrapper) value).unwrap();
            if (unwrapped != value) {
                return toSerializableValue(unwrapped);
            }
        }
        if (value instanceof NativeArray) {
            NativeArray array = (NativeArray) value;
            int length = (int) array.getLength();
            List<Object> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(toSerializableValue(array.get(i, array)));
            }
            return list;
        }
        if (value instanceof Scriptable) {
            Scriptable scriptable = (Scriptable) value;
            Map<String, Object> map = new LinkedHashMap<>();
            for (Object id : scriptable.getIds()) {
                if (id instanceof String) {
                    String key = (String) id;
                    Object member = ScriptableObject.getProperty(scriptable, key);
                    if (member == Undefined.instance) {
                        continue;
                    }
                    map.put(key, toSerializableValue(member));
                }
            }
            return map;
        }
        if (value instanceof Double) {
            Double number = (Double) value;
            if (Double.isNaN(number) == false
                    && Double.isInfinite(number) == false
                    && number.doubleValue() == Math.rint(number.doubleValue())) {
                long longValue = number.longValue();
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return Integer.valueOf((int) longValue);
                }
                return Long.valueOf(longValue);
            }
        }
        return value;
    }

    private static Object firstArg(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        return args[0];
    }

    private static String quoteJavaScriptStringLiteral(String value) {
        return '"' + EscapeUtil.escapeJavaScriptString(value) + '"';
    }
}
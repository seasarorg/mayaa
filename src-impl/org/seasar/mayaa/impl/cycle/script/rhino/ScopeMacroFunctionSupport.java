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

import java.lang.reflect.Array;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
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
        Object value = firstArg(args);
        String json = serializeJsonValue(value, new IdentityHashMap<Object, Boolean>());
        return json != null ? json : "null";
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

    private static String serializeJsonValue(Object value, IdentityHashMap<Object, Boolean> stack) {
        if (value == null || value == Undefined.instance) {
            return "null";
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return quoteJavaScriptStringLiteral(String.valueOf(value));
        }
        if (value instanceof Number) {
            return serializeNumber((Number) value);
        }
        if (value instanceof Boolean) {
            return Context.toString(value);
        }
        if (value instanceof Function) {
            return null;
        }
        if (value instanceof Wrapper) {
            Object unwrapped = ((Wrapper) value).unwrap();
            if (unwrapped != value) {
                return serializeJsonValue(unwrapped, stack);
            }
        }
        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            return serializeArray(value, stack);
        }
        if (value instanceof Iterable) {
            return serializeIterable((Iterable<?>) value, stack);
        }
        if (value instanceof Map) {
            return serializeMap((Map<?, ?>) value, stack);
        }
        if (value instanceof NativeArray) {
            return serializeNativeArray((NativeArray) value, stack);
        }
        if (value instanceof Scriptable) {
            return serializeScriptableObject((Scriptable) value, stack);
        }
        return quoteJavaScriptStringLiteral(Context.toString(value));
    }

    private static String serializeNumber(Number value) {
        if (value instanceof Double || value instanceof Float) {
            double doubleValue = value.doubleValue();
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return "null";
            }
        }
        return Context.toString(value);
    }

    private static String serializeArray(Object array, IdentityHashMap<Object, Boolean> stack) {
        if (stack.put(array, Boolean.TRUE) != null) {
            throw new IllegalStateException("Circular structure cannot be converted to JSON.");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                String item = serializeJsonValue(Array.get(array, i), stack);
                builder.append(item != null ? item : "null");
            }
            builder.append(']');
            return builder.toString();
        } finally {
            stack.remove(array);
        }
    }

    private static String serializeIterable(Iterable<?> iterable, IdentityHashMap<Object, Boolean> stack) {
        if (stack.put(iterable, Boolean.TRUE) != null) {
            throw new IllegalStateException("Circular structure cannot be converted to JSON.");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            boolean first = true;
            for (Iterator<?> it = iterable.iterator(); it.hasNext();) {
                if (!first) {
                    builder.append(',');
                }
                String item = serializeJsonValue(it.next(), stack);
                builder.append(item != null ? item : "null");
                first = false;
            }
            builder.append(']');
            return builder.toString();
        } finally {
            stack.remove(iterable);
        }
    }

    private static String serializeMap(Map<?, ?> map, IdentityHashMap<Object, Boolean> stack) {
        if (stack.put(map, Boolean.TRUE) != null) {
            throw new IllegalStateException("Circular structure cannot be converted to JSON.");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String serializedValue = serializeJsonObjectPropertyValue(entry.getValue(), stack);
                if (serializedValue == null) {
                    continue;
                }
                if (!first) {
                    builder.append(',');
                }
                builder.append(quoteJavaScriptStringLiteral(String.valueOf(entry.getKey())));
                builder.append(':');
                builder.append(serializedValue);
                first = false;
            }
            builder.append('}');
            return builder.toString();
        } finally {
            stack.remove(map);
        }
    }

    private static String serializeNativeArray(NativeArray array, IdentityHashMap<Object, Boolean> stack) {
        if (stack.put(array, Boolean.TRUE) != null) {
            throw new IllegalStateException("Circular structure cannot be converted to JSON.");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            long length = array.getLength();
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    builder.append(',');
                }
                Object item = array.get(i, array);
                String serializedValue = serializeJsonValue(item, stack);
                builder.append(serializedValue != null ? serializedValue : "null");
            }
            builder.append(']');
            return builder.toString();
        } finally {
            stack.remove(array);
        }
    }

    private static String serializeScriptableObject(Scriptable object, IdentityHashMap<Object, Boolean> stack) {
        if (stack.put(object, Boolean.TRUE) != null) {
            throw new IllegalStateException("Circular structure cannot be converted to JSON.");
        }
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('{');
            boolean first = true;
            Object[] ids = object.getIds();
            for (int i = 0; i < ids.length; i++) {
                Object id = ids[i];
                Object property = getProperty(object, id);
                String serializedValue = serializeJsonObjectPropertyValue(property, stack);
                if (serializedValue == null) {
                    continue;
                }
                if (!first) {
                    builder.append(',');
                }
                builder.append(quoteJavaScriptStringLiteral(String.valueOf(id)));
                builder.append(':');
                builder.append(serializedValue);
                first = false;
            }
            builder.append('}');
            return builder.toString();
        } finally {
            stack.remove(object);
        }
    }

    private static Object getProperty(Scriptable object, Object id) {
        if (id instanceof Number) {
            return object.get(((Number) id).intValue(), object);
        }
        return object.get(String.valueOf(id), object);
    }

    private static String serializeJsonObjectPropertyValue(
            Object value, IdentityHashMap<Object, Boolean> stack) {
        if (value == Scriptable.NOT_FOUND || value == Undefined.instance || value instanceof Function) {
            return null;
        }
        if (value instanceof Wrapper) {
            Object unwrapped = ((Wrapper) value).unwrap();
            if (unwrapped != value) {
                return serializeJsonObjectPropertyValue(unwrapped, stack);
            }
        }
        return serializeJsonValue(value, stack);
    }
}
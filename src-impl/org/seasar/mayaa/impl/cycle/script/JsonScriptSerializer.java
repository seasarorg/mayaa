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
package org.seasar.mayaa.impl.cycle.script;

import java.lang.reflect.Array;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.impl.util.EscapeUtil;

/**
 * JSON serialization utility shared across script engines (Rhino, GraalJS).
 */
public final class JsonScriptSerializer {

    private JsonScriptSerializer() {
        // no instantiation
    }

    public static String serialize(Object value) {
        String json = serializeJsonValue(value, new IdentityHashMap<Object, Boolean>());
        return json != null ? json : "null";
    }

    private static String serializeJsonValue(Object value, IdentityHashMap<Object, Boolean> stack) {
        if (value == null) {
            return "null";
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return quoteJavaScriptStringLiteral(String.valueOf(value));
        }
        if (value instanceof Number) {
            return serializeNumber((Number) value);
        }
        if (value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Iterable) {
            return serializeIterable((Iterable<?>) value, stack);
        }
        if (value instanceof Map) {
            return serializeMap((Map<?, ?>) value, stack);
        }
        Class<?> valueClass = value.getClass();
        if (valueClass.isArray()) {
            return serializeArray(value, stack);
        }
        return quoteJavaScriptStringLiteral(value.toString());
    }

    private static String quoteJavaScriptStringLiteral(String value) {
        return '"' + EscapeUtil.escapeJavaScriptString(value) + '"';
    }

    private static String serializeNumber(Number value) {
        if (value instanceof Double || value instanceof Float) {
            double doubleValue = value.doubleValue();
            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                return "null";
            }
        }
        return value.toString();
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
                if (!first) {
                    builder.append(',');
                }
                String key = quoteJavaScriptStringLiteral(String.valueOf(entry.getKey()));
                String value = serializeJsonValue(entry.getValue(), stack);
                builder.append(key).append(':').append(value != null ? value : "null");
                first = false;
            }
            builder.append('}');
            return builder.toString();
        } finally {
            stack.remove(map);
        }
    }
}

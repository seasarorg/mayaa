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
package org.seasar.mayaa.impl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import org.seasar.mayaa.cycle.scope.AttributeScope;

/**
 * 配列やEnumeration, Collectionなどを共通で扱ってIteratorを取得するためのユーティリティ。
 * AttributeScopeの場合はiterateAttributeNames()の結果を返す。
 * Mapの場合はentrySet()のiterator()を返す。
 * Iteratorを取得できないオブジェクトの場合は、それ自体をObject配列に入れた
 * もののIteratorを返す。ただしnullの場合はNullIteratorを返す。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class IteratorUtil {

    private IteratorUtil() {
        // no instantiation.
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Iterator<?> toIterator(Object o) {
        if (o instanceof AttributeScope) {
            return ((AttributeScope)o).iterateAttributeNames();
        } else if (o instanceof Collection) {
            return ((Collection<?>) o).iterator();
        } else if (o instanceof Iterator) {
            return (Iterator<?>) o;
        } else if (o instanceof Enumeration) {
            return new EnumrationIterator<>((Enumeration) o, o.getClass());
        } else if (o instanceof Map) {
            return ((Map<?, ?>) o).entrySet().iterator();
        } else if (o instanceof Object[]) {
            return toObjectIterator((Object[]) o);
        } else if (o instanceof boolean[]) {
            return toObjectIterator((boolean[]) o);
        } else if (o instanceof byte[]) {
            return toObjectIterator((byte[]) o);
        } else if (o instanceof char[]) {
            return toObjectIterator((char[]) o);
        } else if (o instanceof short[]) {
            return toObjectIterator((short[]) o);
        } else if (o instanceof int[]) {
            return toObjectIterator((int[]) o);
        } else if (o instanceof long[]) {
            return toObjectIterator((long[]) o);
        } else if (o instanceof float[]) {
            return toObjectIterator((float[]) o);
        } else if (o instanceof double[]) {
            return toObjectIterator((double[]) o);
        } else if (o != null) {
            return toObjectIterator(new Object[] { o });
        }

        return Collections.emptyIterator();
    }

    private static Iterator<Object> toObjectIterator(Object[] objects) {
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Boolean> toObjectIterator(boolean[] booleans) {
        Boolean[] objects = new Boolean[booleans.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = Boolean.valueOf(booleans[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Byte> toObjectIterator(byte[] bytes) {
        Byte[] objects = new Byte[bytes.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Byte(bytes[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Character> toObjectIterator(char[] chars) {
        Character[] objects = new Character[chars.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Character(chars[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Short> toObjectIterator(short[] shorts) {
        Short[] objects = new Short[shorts.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Short(shorts[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Integer> toObjectIterator(int[] ints) {
        Integer[] objects = new Integer[ints.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Integer(ints[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Long> toObjectIterator(long[] longs) {
        Long[] objects = new Long[longs.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Long(longs[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Float> toObjectIterator(float[] floats) {
        Float[] objects = new Float[floats.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Float(floats[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator<Double> toObjectIterator(double[] doubles) {
        Double[] objects = new Double[doubles.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Double(doubles[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static class EnumrationIterator<T> implements Iterator<T> {

        private Enumeration<T> _enumeration;

        protected EnumrationIterator(Enumeration<T> enumeration, Class<T> clazz) {
            _enumeration = (Enumeration<T>) enumeration;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return _enumeration.hasMoreElements();
        }

        public T next() {
            return _enumeration.nextElement();
        }

    }

}

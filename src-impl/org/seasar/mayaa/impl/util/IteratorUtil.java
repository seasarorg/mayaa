package org.seasar.mayaa.impl.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class IteratorUtil {

    public static final Iterator NULL_ITERATOR = new Iterator() {
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new NoSuchElementException();
        }
    };

    public static Iterator toIterator(Object o) {
        if (o instanceof Collection) {
            return ((Collection) o).iterator();
        } else if (o instanceof Iterator) {
            return (Iterator) o;
        } else if (o instanceof Enumeration) {
            return new EnumrationIterator((Enumeration) o);
        } else if (o instanceof Map) {
            return ((Map) o).entrySet().iterator();
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

        return NULL_ITERATOR;
    }

    private static Iterator toObjectIterator(Object[] objects) {
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(boolean[] booleans) {
        Boolean[] objects = new Boolean[booleans.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Boolean(booleans[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(byte[] bytes) {
        Byte[] objects = new Byte[bytes.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Byte(bytes[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(char[] chars) {
        Character[] objects = new Character[chars.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Character(chars[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(short[] shorts) {
        Short[] objects = new Short[shorts.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Short(shorts[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(int[] ints) {
        Integer[] objects = new Integer[ints.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Integer(ints[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(long[] longs) {
        Long[] objects = new Long[longs.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Long(longs[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(float[] floats) {
        Float[] objects = new Float[floats.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Float(floats[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static Iterator toObjectIterator(double[] doubles) {
        Double[] objects = new Double[doubles.length];
        for (int i = 0; i < objects.length; i++) {
            objects[i] = new Double(doubles[i]);
        }
        return Arrays.asList(objects).iterator();
    }

    private static class EnumrationIterator implements Iterator {

        private Enumeration _enumeration;

        private EnumrationIterator(Enumeration enumeration) {
            _enumeration = enumeration;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return _enumeration.hasMoreElements();
        }

        public Object next() {
            return _enumeration.nextElement();
        }

    }

}

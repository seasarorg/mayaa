/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.test.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TestObjectFactory {

    public static Map<Number, String> createNumberKeyMap() {
        Map<Number, String> map = new MyLinkedHashMap<>();

        map.put(Byte.valueOf((byte) 10), "foo_b");
        map.put(Short.valueOf((short) 11), "foo_s");
        map.put(Integer.valueOf(12), "foo_i");
        map.put(Long.valueOf(13), "foo_l");
        map.put(Float.valueOf(10.1f), "foo_f");
        map.put(Double.valueOf(10.2), "foo_d");
        map.put(new BigInteger("20"), "bar_i");
        map.put(new BigDecimal("20.1"), "bar_d");

        return map;
    }

    public static class MyLinkedHashMap<K, V> extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = -163357172174710647L;

        public V get(Object key) {
            return super.get(key);
        }
    }

    public static List<Number> createNumberKeys() {
        List<Number> keys = new ArrayList<>();

        keys.add(Byte.valueOf((byte) 10));
        keys.add(Short.valueOf((short) 11));
        keys.add(Integer.valueOf(12));
        keys.add(Long.valueOf(13));
        keys.add(Float.valueOf(10.1f));
        keys.add(Double.valueOf(10.2));
        keys.add(new BigInteger("20"));
        keys.add(new BigDecimal("20.1"));

        return keys;
    }

    public static Object[] createArray() {
        Object[] array = new Object[3];
        array[0] = Integer.valueOf(1);
        array[1] = "foo";
        array[2] = new NameObject("bar");
        return array;
    }

    public static CompositeList createCompositeList() {
        CompositeList list = new CompositeList("1");
        list.addChild(new CompositeList("1-1"));
        list.addChild(new CompositeList("1-2"));

        CompositeList list2 = new CompositeList("1-3");
        list2.addChild(new CompositeList("1-3-1"));
        list2.addChild(new CompositeList("1-3-2"));

        CompositeList list3 = new CompositeList("1-3-3");
        list3.addChild(new CompositeList("1-3-3-1"));
        list2.addChild(list3);

        list.addChild(list2);

        return list;
    }

    public static class NameObject {
        private String _name;

        public NameObject(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }

        public String toString() {
            return _name;
        }
    }

    public static class CompositeList {
        private String _name;
        private List<CompositeList> _children = new ArrayList<>();

        public CompositeList(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }

        public boolean hasChild() {
            return _children.size() > 0;
        }

        public void addChild(CompositeList child) {
            _children.add(child);
        }

        public List<CompositeList> getChildren() {
            return _children;
        }
    }

    public static abstract class AbstractNameObject {
        private String _name;
        private String _QName;
        public String publicName;

        public static AbstractNameObject createPublicInstance(String name, String QName) {
            return new PublicNameObject(name, QName);
        }

        public static AbstractNameObject createPrivateInstance(String name, String QName) {
            return new PrivateNameObject(name, QName);
        }

        public static AbstractNameObject createProtectedInstance(String name, String QName) {
            return new ProtectedNameObject(name, QName);
        }

        public static AbstractNameObject createDefaultInstance(String name, String QName) {
            return new DefaultNameObject(name, QName);
        }

        protected AbstractNameObject(String name, String QName) {
            _name = name;
            _QName = QName;
            publicName = _name;
        }

        public String getName() {
            return _name;
        }

        public String getQName() {
            return _QName;
        }

        public String isNotName() {
            return getPrivateName();
        }

        public boolean isMyName() {
            return true;
        }

        public Boolean isYourName() {
            return Boolean.TRUE;
        }

        private String getPrivateName() {
            return _name;
        }

        protected String getProtectedName() {
            return _name;
        }

        String getDefaultName() {
            return _name;
        }
    }

    public static class PublicNameObject extends AbstractNameObject {
        public PublicNameObject(String name, String QName) {
            super(name, QName);
        }
    }

    private static class PrivateNameObject extends AbstractNameObject {
        public PrivateNameObject(String name, String QName) {
            super(name, QName);
        }
    }

    protected static class ProtectedNameObject extends AbstractNameObject {
        public ProtectedNameObject(String name, String QName) {
            super(name, QName);
        }
    }

    static class DefaultNameObject extends AbstractNameObject {
        public DefaultNameObject(String name, String QName) {
            super(name, QName);
        }
    }

}

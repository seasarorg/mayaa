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

import java.io.InputStream;

import org.seasar.mayaa.impl.util.xml.XMLReaderPool;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public final class XMLUtil {

    private XMLUtil() {
        // no instantiation.
    }

    public static void parse(DefaultHandler handler,
            InputStream stream, String publicID,
            String systemID, boolean namespace,
            boolean validation, boolean xmlSchema) {
        if (stream == null) {
            throw new IllegalArgumentException();
        }
        XMLReaderPool pool = XMLReaderPool.getPool();
        XMLReader xmlReader = pool.borrowXMLReader(
                handler, namespace, validation, xmlSchema);
        InputSource input = new InputSource(stream);
        input.setPublicId(publicID);
        input.setSystemId(systemID);
        try {
            xmlReader.parse(input);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            throw new RuntimeException(t.getMessage(), t);
        } finally {
            pool.returnXMLReader(xmlReader);
        }
    }

    public static String getStringValue(
            Attributes attr, String localName, String defaultValue) {
        String value = attr.getValue(localName);
        if (StringUtil.hasValue(value)) {
            return value;
        }
        return defaultValue;
    }

    public static int getIntValue(
            Attributes attr, String localName, int defaultValue) {
        String value = attr.getValue(localName);
        if (StringUtil.hasValue(value)) {
            return Integer.parseInt(value);
        }
        return defaultValue;
    }

    public static boolean getBooleanValue(
            Attributes attr, String localName, boolean defaultValue) {
        String value = attr.getValue(localName);
        return ObjectUtil.booleanValue(value, defaultValue);
    }

    public static Class getClassValue(
            Attributes attr, String localName, Class defaultValue) {
        String className = attr.getValue(localName);
        if (StringUtil.hasValue(className)) {
            return ObjectUtil.loadClass(className);
        }
        return defaultValue;
    }

    public static Object getObjectValue(
            Attributes attr, String localName, Class expectedClass) {
        if (attr == null || StringUtil.isEmpty(localName)
                || expectedClass == null) {
            throw new IllegalArgumentException();
        }
        Class clazz = getClassValue(attr, localName, null);
        if (clazz != null) {
            if (expectedClass.isAssignableFrom(clazz)) {
                return ObjectUtil.newInstance(clazz);
            }
            throw new IllegalClassTypeException(expectedClass, clazz);
        }
        return null;
    }

}

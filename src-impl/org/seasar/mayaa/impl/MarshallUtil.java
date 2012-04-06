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
package org.seasar.mayaa.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.seasar.mayaa.PositionAware;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.impl.source.URLSourceDescriptor;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.LIFOIterator;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MarshallUtil {

    private MarshallUtil() {
        // no instantiate.
    }

    protected static void setPosition(
            Object obj, String systemID, int lineNumber) {
        if (obj == null || StringUtil.isEmpty(systemID) || lineNumber < 0) {
            throw new IllegalArgumentException();
        }
        if (obj instanceof PositionAware) {
            PositionAware positionAware = (PositionAware) obj;
            positionAware.setSystemID(systemID);
            positionAware.setLineNumber(lineNumber);
        }
    }

    public static Object marshall(Class instanceClass, Class interfaceClass,
            Object beforeObject, String systemID, int lineNumber) {
        if (instanceClass == null) {
            if (beforeObject == null) {
                throw new IllegalArgumentException();
            }
            return beforeObject;
        }
        if (beforeObject != null) {
            Constructor constructor = ObjectUtil.getConstructor(
                    instanceClass, new Class[] { interfaceClass });
            if (constructor != null) {
                Object obj = ObjectUtil.newInstance(
                        constructor, new Object[] { beforeObject });
                setPosition(obj, systemID, lineNumber);
                return obj;
            }
        }
        Object obj = ObjectUtil.newInstance(instanceClass);
        setPosition(obj, systemID, lineNumber);
        return obj;
    }

    public static SourceDescriptor getDefaultSource(
            String systemID, Class neighborClass) {
        if (StringUtil.isEmpty(systemID) || neighborClass == null) {
            throw new IllegalArgumentException();
        }
        ClassLoaderSourceDescriptor defaultSource =
            new ClassLoaderSourceDescriptor();
        defaultSource.setSystemID(systemID);
        defaultSource.setNeighborClass(neighborClass);
        return defaultSource;
    }

    public static Iterator iterateMetaInfSources(String systemID) {
        if (StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        if (systemID.startsWith("META-INF/") == false) {
            systemID = "META-INF" + StringUtil.preparePath(systemID);
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration resources = loader.getResources(systemID);
            return new URLSourceIterator(
                    new LIFOIterator(resources), systemID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // support class ------------------------------------------------

    protected static class URLSourceIterator implements Iterator {

        private Iterator _it;
        private String _systemID;

        public URLSourceIterator(Iterator it, String systemID) {
            if (it == null || StringUtil.isEmpty(systemID)) {
                throw new IllegalArgumentException();
            }
            LinkedHashSet urlSet = new LinkedHashSet();
            while (it.hasNext()) {
                URL next = (URL) it.next();
                urlSet.add(next);
            }
            _it = urlSet.iterator();
            _systemID = systemID;
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            URL url = (URL) _it.next();
            URLSourceDescriptor urlSource = new URLSourceDescriptor();
            urlSource.setURL(url);
            urlSource.setSystemID(_systemID);
            return urlSource;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

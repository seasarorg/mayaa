/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.source.URLSourceDescriptor;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.LIFOIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MarshallUtil {

    private MarshallUtil() {
        // no instantiate.
    }

    public static Object marshall(Class instanceClass, 
            Class interfaceClass, Object beforeObject) {
        if(instanceClass == null) {
            return beforeObject;
        }
        if(beforeObject != null) {
            Constructor constructor = ObjectUtil.getConstructor(
                    instanceClass, new Class[] { interfaceClass });
            if(constructor != null) {
                return ObjectUtil.newInstance(
                        constructor, new Object[] { beforeObject });
            }
        }
        return ObjectUtil.newInstance(instanceClass);
    }
    
    public static SourceDescriptor getDefaultSource(
            String systemID, Class neighborClass) {
        if(StringUtil.isEmpty(systemID) || neighborClass == null) {
            throw new IllegalArgumentException();
        }
        ClassLoaderSourceDescriptor defaultSource =
            new ClassLoaderSourceDescriptor();
        defaultSource.setSystemID(systemID);
        defaultSource.setNeighborClass(neighborClass);
        return defaultSource;
    }
    
    public static Iterator iterateMetaInfSources(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        if(systemID.startsWith("META-INF/") == false) {
            systemID = "META-INF" + StringUtil.preparePath(systemID);
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration resources =
                loader.getResources(systemID);
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
            if(it == null || StringUtil.isEmpty(systemID)) {
                throw new IllegalArgumentException();
            }
            _it = it;
            _systemID = systemID;
        }
        
        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            URL url = (URL)_it.next();
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

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
package org.seasar.maya.impl.builder.library.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.IllegalParameterValueException;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.IOUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class MetaInfSourceScanner extends ParameterAwareImpl
		implements SourceScanner {

    private static Map _cache = new HashMap();

    private FolderSourceScanner _folderScanner =
        new FolderSourceScanner();
    private Set _ignores = new HashSet();

    protected String getJarName(String systemID) {
        if (StringUtil.hasValue(systemID)) {
            int pos = systemID.lastIndexOf("/");
            if (pos != -1) {
                return systemID.substring(pos + 1);
            }
            return systemID;
        }
        return null;
    }

    protected boolean containIgnores(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = _ignores.iterator(); it.hasNext();) {
            if (name.startsWith(it.next().toString())) {
                return true;
            }
        }
        return false;
    }

    protected void scanSource(SourceDescriptor source, Set aliases) {
        if (source == null && source.exists() == false) {
            throw new IllegalArgumentException();
        }
        String jarName = getJarName(source.getSystemID());
        if (StringUtil.hasValue(jarName) &&
                containIgnores(jarName) == false) {
            InputStream stream = source.getInputStream();
            try {
                JarInputStream jar = new JarInputStream(stream);
                JarEntry entry;
                while ((entry = jar.getNextJarEntry()) != null) {
                    String entryName = entry.getName();
                    if (entryName.startsWith("META-INF/")) {
                        if("META-INF/MANIFEST.MF".equals(
                                entryName) == false) {
                            aliases.add(new SourceAlias(
                                    jarName, entryName, source.getTimestamp()));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtil.close(stream);
            }
        }
    }

    protected Set scanAll(SourceScanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException();
        }
        Set aliases = new HashSet();
        for (Iterator it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            scanSource(source, aliases);
        }
        return aliases;
    }

    public Iterator scan() {
        String folder = _folderScanner.getFolder();
        Set aliases = (Set) _cache.get(folder);
        if (aliases == null) {
            aliases = scanAll(_folderScanner);
            _cache.put(folder, aliases);
        }
        return new MetaInfSourceIterator(aliases.iterator());
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("ignore".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _ignores.add(value);
        } else {
            _folderScanner.setParameter(name, value);
        }
        super.setParameter(name, value);
    }

    // suppport class -----------------------------------------------

    protected class MetaInfSourceIterator implements Iterator {

        private Iterator _it;

        public MetaInfSourceIterator(Iterator it) {
            if (it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            SourceAlias alias = (SourceAlias) _it.next();
            ClassLoaderSourceDescriptor source =
                new ClassLoaderSourceDescriptor();
            source.setSystemID(alias.getSystemID());
            source.setParameter(SourceAlias.ALIAS, alias.getAlias());
            source.setTimestamp(alias.getTimestamp());
            return source;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

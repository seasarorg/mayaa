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
package org.seasar.mayaa.impl.builder.library.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.impl.NonSerializableParameterAwareImpl;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class JarSourceScanner extends NonSerializableParameterAwareImpl implements
        SourceScanner {
    private static final Log LOG = LogFactory.getLog(JarSourceScanner.class.getName());

    private String _root;
    private SourceDescriptor _descriptor;
    private Set<String> _folderFilters = new HashSet<>();
    private Set<String> _ignores = new HashSet<>();
    private Set<String> _extensions = new HashSet<>();

    public Iterator<SourceDescriptor> scan() {
        if (_descriptor == null) {
            return Collections.emptyIterator();
        }
        Set<SourceAlias> aliases = new HashSet<>();

        LOG.debug("SCANNING " + _descriptor.getSystemID());
        scanSource(_descriptor, aliases);
        return new AliasToSourceIterator(aliases.iterator());
    }

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

    protected void scanSource(SourceDescriptor source, Set<SourceAlias> aliases) {
        if (source == null || source.exists() == false) {
            throw new IllegalArgumentException();
        }
        String jarName = getJarName(source.getSystemID());
        if (StringUtil.hasValue(jarName)) {
            String jarPath = ((_root != null) ? _root : "") + source.getSystemID();
            Date timestamp = source.getTimestamp();

            InputStream stream = null;
            JarInputStream jar = null;
            try {
                stream = source.getInputStream();
                jar = new JarInputStream(stream);
                addAliases(jar, jarPath, timestamp, aliases);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (jar != null) {
                    IOUtil.close(jar);
                } else {
                    IOUtil.close(stream);
                }
            }
        }
    }

    protected void addAliases(
            JarInputStream jar, String jarPath, Date timestamp, Set<SourceAlias> aliases)
            throws IOException {
        JarEntry entry;
        while ((entry = jar.getNextJarEntry()) != null) {
            String entryName = entry.getName();
            if (!isTargetEntry(entryName)) {
                continue;
            }

            if (isIgnored(entryName)) {
                LOG.debug("SKIP    " + jarPath + "!" + entryName);
                continue;
            }
            LOG.debug("READING " + jarPath + "!" + entryName);
            aliases.add(new SourceAlias(jarPath, entryName, timestamp));
        }
    }

    protected boolean isTargetEntry(String entryName) {
        if (!extensionEndsWith(entryName)) {
            return false;
        }
        if (_folderFilters.size() > 0) {
            boolean ok = false;
            for (String filter : _folderFilters) {
                if (entryName.startsWith(filter)) {
                    ok = true;
                }
            }
            if (!ok) {
                return false;
            }
        }
        return true;
    }

    protected boolean isIgnored(String entryName) {
        if (_ignores.size() > 0) {
            for (String filter : _ignores) {
                if (entryName.startsWith(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean extensionEndsWith(String entryName) {
        if (_extensions.size() == 0) {
            return true;        // all
        }
        for (String filter : _extensions) {
            if (entryName.endsWith(filter)) {
                return true;
            }
        }
        return false;
    }

    public void setDescriptor(SourceDescriptor descriptor) {
        _descriptor = descriptor;
    }

    protected String getFileName(String path) {
        int separatorIndex = path.lastIndexOf('/');
        if (separatorIndex > 0) {
            path = path.substring(separatorIndex+1);
        } else if (File.separatorChar != '/') {
            separatorIndex = path.lastIndexOf(File.separatorChar);
            if (separatorIndex > 0) {
                path = path.substring(separatorIndex+1);
            }
        }
        return path;
    }

    //  Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("root".equals(name)) {
            _root = value;
        } else if ("folder".equals(name)) {
            _folderFilters.add(value);
        } else if ("ignore".equals(name)) {
            _ignores.add(value);
        } else if ("extension".equals(name)) {
            _extensions.add(value);
        }
        super.setParameter(name, value);
    }

    // suppport class -----------------------------------------------

    protected static class AliasToSourceIterator implements Iterator<SourceDescriptor> {

        private Iterator<SourceAlias> _it;

        public AliasToSourceIterator(Iterator<SourceAlias> it) {
            if (it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public SourceDescriptor next() {
            SourceAlias alias = _it.next();
            ClassLoaderSourceDescriptor source =
                new ClassLoaderSourceDescriptor();
            source.setSystemID(alias.getSystemID());
            source.setAlias(alias);
            source.setTimestamp(alias.getTimestamp());
            return source;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}


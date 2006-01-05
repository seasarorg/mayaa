/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.cycle.scope.ApplicationScope;
import org.seasar.mayaa.impl.IllegalParameterValueException;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FolderSourceScanner extends ParameterAwareImpl
        implements SourceScanner {

    private ApplicationSourceDescriptor _source;

    private String _folder;

    private boolean _recursive = false;

    private Set _extensions = new HashSet();
    private Set _unmodifiableExtensions =
        Collections.unmodifiableSet(_extensions);

    public String getFolder() {
        if (StringUtil.isEmpty(_folder)) {
            throw new IllegalStateException();
        }
        return _folder;
    }

    public boolean isRecursive() {
        return _recursive;
    }

    public Set getExtensions() {
        return _unmodifiableExtensions;
    }

    protected String[] getExtensionArray() {
        return (String[])_extensions.toArray(new String[_extensions.size()]);
    }

    public Iterator scan() {
        if (_source == null) {
            _source = new ApplicationSourceDescriptor();
            _source.setDenyWebInf(false);
            _source.setRoot(getFolder());
        }
        if (_source.exists() && _source.getFile().isDirectory()) {
            return new FileToSourceIterator(_source.getApplicationScope(),
                    _source.getRoot(), iterateFiles(_source.getFile()));
        }
        return NullIterator.getInstance();
    }

    protected Iterator iterateFiles(File dir) {
        if (dir.exists()) {
            File[] files;
            if (dir.isDirectory()) {
                files = listFiles(dir);
            } else {
                files = new File[] { dir };
            }
            return new FileArrayIterator(files);
        }
        return NullIterator.getInstance();
    }

    protected FileFilter createExtensionFilter() {
        return new FileFilter() {
            String[] extensions = getExtensionArray();
            public boolean accept(File pathName) {
                if (pathName.isDirectory()) {
                    return true;
                }

                for (int i = 0; i < extensions.length; i++) {
                    if (pathName.getName().endsWith(extensions[i])) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    protected File[] listFiles(File dir) {
        List sources = new ArrayList();
        FileFilter filter = createExtensionFilter();

        if (_recursive) {
            listFilesRecursive(sources, dir, filter);
        } else {
            listFilesNonRecursive(sources, dir, filter);
        }
        return (File[]) sources.toArray(new File[sources.size()]);
    }

    protected void listFilesNonRecursive(List list, File dir, FileFilter filter) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(filter);
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    list.add(files[i]);
                }
            }
        }
    }

    protected void listFilesRecursive(List list, File dir, FileFilter filter) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(filter);
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    listFilesRecursive(list, files[i], filter);
                } else {
                    list.add(files[i]);
                }
            }
        }
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("folder".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            _folder = value;
        } else if ("recursive".equals(name)) {
            _recursive = Boolean.valueOf(value).booleanValue();
        } else if ("extension".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(getClass(), name);
            }
            if (value.charAt(0) != '.') {
                _extensions.add('.' + value);
            } else {
                _extensions.add(value);
            }
        }
        super.setParameter(name, value);
    }

    // support class ------------------------------------------------

    protected static class FileArrayIterator implements Iterator {

        private File[] _files;

        private int _index;

        public FileArrayIterator(File[] files) {
            if (files == null) {
                throw new IllegalArgumentException();
            }
            _files = files;
        }

        public boolean hasNext() {
            return _index < _files.length;
        }

        public Object next() {
            if (hasNext() == false) {
                throw new NoSuchElementException();
            }
            return _files[_index++];
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    protected class FileToSourceIterator implements Iterator {

        private ApplicationScope _applicationScope;
        private String _root;
        private Iterator _iterator;

        public FileToSourceIterator(ApplicationScope applicationScope,
                String root, Iterator iterator) {
            if (applicationScope == null || iterator == null) {
                throw new IllegalArgumentException();
            }
            _applicationScope = applicationScope;
            _root = root;
            _iterator = iterator;
        }

        public boolean hasNext() {
            return _iterator.hasNext();
        }

        private String getSystemID(File file) {
            String sourceRoot = _root;
            if (StringUtil.isEmpty(sourceRoot)) {
                sourceRoot = "/";
            }
            String root = _applicationScope.getRealPath(sourceRoot);
            String absolutePath = file.getAbsolutePath();
            String path = absolutePath.substring(root.length());
            return StringUtil.preparePath(path);
        }

        public Object next() {
            Object ret = _iterator.next();
            if (ret instanceof File) {
                File file = (File) ret;
                String systemID = getSystemID(file);
                ApplicationSourceDescriptor source =
                    new ApplicationSourceDescriptor();
                source.setDenyWebInf(false);
                source.setRoot(_root);
                source.setSystemID(systemID);
                source.setFile(file);
                return source;
            }
            throw new IllegalStateException();
        }

        public void remove() {
            _iterator.remove();
        }

    }

}

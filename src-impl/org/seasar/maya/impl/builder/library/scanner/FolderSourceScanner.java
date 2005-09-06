/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.builder.library.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.provider.IllegalParameterValueException;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.source.ApplicationSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FolderSourceScanner implements SourceScanner {

    private ApplicationSourceDescriptor _source;

    private String _folder;

    private boolean _recursive = false;

    public void setParameter(String name, String value) {
        if ("folder".equals(name)) {
            if (StringUtil.isEmpty(value)) {
                throw new IllegalParameterValueException(name);
            }
            _folder = value;
        } else if ("recursive".equals(name)) {
            _recursive = Boolean.valueOf(value).booleanValue();
        } else {
            throw new UnsupportedParameterException(name);
        }
    }

    public String getFolder() {
        if (StringUtil.isEmpty(_folder)) {
            throw new IllegalStateException();
        }
        return _folder;
    }

    public Iterator scan() {
        if (_source == null) {
            _source = new ApplicationSourceDescriptor();
            _source.setRoot(getFolder());
        }
        if (_source.exists() && _source.getFile().isDirectory()) {
            return new FileToSourceIterator(iterateFiles(_source.getFile()));
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

    private File[] listFiles(File dir) {
        List sources = new ArrayList();
        if (_recursive) {
            listFilesRecursive(sources, dir);
        } else {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    sources.add(files[i]);
                }
            }
        }
        return (File[]) sources.toArray(new File[sources.size()]);
    }

    private void listFilesRecursive(List list, File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    listFilesRecursive(list, files[i]);
                } else {
                    list.add(files[i]);
                }
            }
        }
    }

    private static class FileArrayIterator implements Iterator {

        private File[] _files;

        private int _index;

        private FileArrayIterator(File[] files) {
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

    private class FileToSourceIterator implements Iterator {

        private Iterator _iterator;

        private FileToSourceIterator(Iterator iterator) {
            if (iterator == null) {
                throw new IllegalArgumentException();
            }
            _iterator = iterator;
        }

        public boolean hasNext() {
            return _iterator.hasNext();
        }

        private String getSystemID(File file) {
            String sourceRoot = _source.getRoot();
            if (StringUtil.isEmpty(sourceRoot)) {
                sourceRoot = "/";
            }
            String root = _source.getApplication().getRealPath(sourceRoot);
            String absolutePath = file.getAbsolutePath();
            String path = absolutePath.substring(root.length());
            return StringUtil.preparePath(path);
        }

        public Object next() {
            Object ret = _iterator.next();
            if (ret instanceof File) {
                File file = (File) ret;
                String systemID = getSystemID(file);
                ApplicationSourceDescriptor source = new ApplicationSourceDescriptor();
                source.setRoot(_source.getRoot());
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

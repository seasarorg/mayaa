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
import java.util.Iterator;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.source.ApplicationSourceDescriptor;
import org.seasar.maya.impl.util.FileUtil;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class InternalApplicationSourceScanner implements SourceScanner {

    private ApplicationSourceDescriptor _source;
    
    public InternalApplicationSourceScanner(ApplicationSourceDescriptor source) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        _source = source;
    }

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public Iterator scan() {
        if(_source.exists() && _source.getFile().isDirectory()) {
            return new FileToSourceIterator(FileUtil.iterateFiles(_source.getFile()));
        }
        return NullIterator.getInstance();
    }

    private class FileToSourceIterator implements Iterator {
        
        private Iterator _iterator;
        
        private FileToSourceIterator(Iterator iterator) {
            if(iterator == null) {
                throw new IllegalArgumentException();
            }
            _iterator = iterator;
        }
        
        public boolean hasNext() {
            return _iterator.hasNext();
        }

        private String getSystemID(File file) {
            String root = _source.getApplication().getRealPath(_source.getRoot());
            String absolutePath = file.getAbsolutePath();
            return absolutePath.substring(root.length());
        }
        
        public Object next() {
            Object ret = _iterator.next();
            if(ret instanceof File) {
                File file = (File)ret;
                String systemID = getSystemID(file);
                ApplicationSourceDescriptor source =
                    new ApplicationSourceDescriptor(_source.getRoot(), systemID);
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

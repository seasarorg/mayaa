/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.impl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.jar.JarInputStream;

import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class FileUtil {

    private FileUtil() {
    }

    public static String getFileNameFromURL(String url) {
        int pos = url.lastIndexOf("/");
        if(pos != -1) {
            url = url.substring(pos);
        }
        if(url.startsWith("/")) {
            url = url.substring(1);
        }
        return url;
    }
    
    public static Iterator iterateFiles(File dir) {
        if(dir.exists()) {
            File[] files;
            if(dir.isDirectory()) {
                files = dir.listFiles();
            } else {
                files = new File[] { dir };
            }
            return new FileArrayIterator(files);
        }
        return NullIterator.getInstance();
    }

    public static Iterator iterateFiles(File dir, String extension) {
        return new FileFilteredIterator(extension, iterateFiles(dir));
    }
    
    public static InputStream getInputStream(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static JarInputStream getJarInputStream(InputStream stream) {
        try {
            return new JarInputStream(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
//    private static class FileArrayEntry {
//
//        private File[] _files;
//        private int _index;
//
//        private FileArrayEntry(File[] files) {
//            if(files == null) {
//                throw new IllegalArgumentException();
//            }
//            _files = files;
//        }
//        
//        public boolean hasNext() {
//            return _index < _files.length;
//        }
//        
//        public Object next() {
//            if(hasNext() == false) {
//                throw new NoSuchElementException();
//            }
//            return _files[_index++];
//        }
//        
//    }
//
//    static class FileArrayIterator2 implements Iterator {
//
//        private Stack _stack;
//        
//        FileArrayIterator2(File[] files) {
//            if(files == null) {
//                throw new IllegalArgumentException();
//            }
//            _stack = new Stack();
//            _stack.push(new FileArrayEntry(files));
//        }
//        
//        public boolean hasNext() {
//            if(_stack.size() == 0) {
//                return false;
//            }
//            FileArrayEntry entry = (FileArrayEntry)_stack.peek();
//            if(entry.hasNext()) {
//                return true;
//            }
//            entry = (FileArrayEntry)_stack.pop();
//        }
//        
//        public Object next() {
//        }
//        
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//        
//    }
    
    private static class FileArrayIterator implements Iterator {
        
        private File[] _files;
        private int _index;
        
        private FileArrayIterator(File[] files) {
            if(files == null) {
                throw new IllegalArgumentException();
            }
            _files = files;
        }
        
        public boolean hasNext() {
            return _index < _files.length;
        }
        
        public Object next() {
            if(hasNext() == false) {
                throw new NoSuchElementException();
            }
            return _files[_index++];
        }
        
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static class FileFilteredIterator extends AbstractScanningIterator {
        
        private String _extension;
        
        private FileFilteredIterator(String extension, Iterator it) {
            super(it);
            if(StringUtil.isEmpty(extension)) {
                throw new IllegalArgumentException();
            }
            if(extension.startsWith(".") == false) {
                extension = "." + extension;
            }
            _extension = extension.toLowerCase();
        }
        
        protected Object getNextObject(Object next) {
            if(next instanceof File) {
                File file = (File)next;
                if(file.isDirectory()) {
                    return new FileFilteredIterator(
                            _extension, new FileArrayIterator(file.listFiles()));
                }
            }
            return next; 
        }

        protected boolean filter(Object test) {
            if(test instanceof File) {
                File file = (File)test;
   				String name = file.getName().toLowerCase();
    			return file.isFile() && name.endsWith(_extension);
            }
            return false;
        }
	}

}

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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.source.SourceDescriptor;

/**
 * TODO noscan.lst ÇóòópÇ∑ÇÈÅB
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class InternalMetaInfSourceScanner implements SourceScanner {

    private SourceScanner _scanner;
    
    public InternalMetaInfSourceScanner(SourceScanner scanner) {
        if(scanner == null) {
            throw new IllegalArgumentException();
        }
        _scanner = scanner;
    }

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }
    
    protected static void scanSource(SourceDescriptor source, Set paths) {
        if(source == null && source.exists() == false) {
            throw new IllegalArgumentException();
        }
        if(source.getSystemID().toLowerCase().endsWith(".jar")) {
            try {
                JarInputStream jar = new JarInputStream(source.getInputStream());
                JarEntry entry;
                while((entry = jar.getNextJarEntry())  != null) {
                    String name = entry.getName();
                    if(name.startsWith("META-INF/") && 
                            "META-INF/MANIFEST.MF".equals(name) == false) {
                        paths.add(name);
                    }
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    protected static Iterator scanAll(SourceScanner scanner) {
        if(scanner == null) {
            throw new IllegalArgumentException();
        }
        Set paths = new HashSet();
        for(Iterator it = scanner.scan(); it.hasNext(); ) {
            SourceDescriptor source = (SourceDescriptor)it.next();
            scanSource(source, paths);
        }
        return paths.iterator();
    }

    public Iterator scan() {
        return new MetaInfSourceIterator(scanAll(_scanner));
	}
    
    private class MetaInfSourceIterator implements Iterator {

        private Iterator _it;
        
        private MetaInfSourceIterator(Iterator it) {
            if(it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }
        
        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            return new ClassLoaderSourceDescriptor(null, (String)_it.next(), null);
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

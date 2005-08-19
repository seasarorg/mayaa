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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class JarsSourceScanner implements SourceScanner {

    private static Map _cache = new HashMap();
    
    private FolderSourceScanner _folderScanner = new FolderSourceScanner();
    private Set _ignores = new HashSet();
    
    public void setParameter(String name, String value) {
        if("ignore".equals(name)) {
            if(StringUtil.isEmpty(value)) {
                // TODO 不正なパラメータの例外。
                throw new IllegalArgumentException();
            }
            _ignores.add(value);
        } else {
            _folderScanner.setParameter(name, value);
        }
    }
    
    protected String getJarName(String systemID) {
        if(StringUtil.hasValue(systemID) && 
        		systemID.toLowerCase().endsWith(".jar")) {
            int pos = systemID.lastIndexOf("/");
            if(pos != -1) {
                return systemID.substring(pos + 1);
            }
            return systemID;
        }
        return null;
    }
    
    protected boolean containIgnores(String name) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        for (Iterator it = _ignores.iterator(); it.hasNext(); ) {
            if (name.startsWith(it.next().toString())) {
                return true;
            }
        }
        return false;
    }
    
    protected void scanSource(SourceDescriptor source, Set aliases) {
        if(source == null && source.exists() == false) {
            throw new IllegalArgumentException();
        }
        String jarName = getJarName(source.getSystemID());
        if(StringUtil.hasValue(jarName) && containIgnores(jarName) == false) {
            try {
                JarInputStream jar = new JarInputStream(source.getInputStream());
                JarEntry entry;
                while((entry = jar.getNextJarEntry())  != null) {
                    String entryName = entry.getName();
                    if(entryName.startsWith("META-INF/") && 
                            "META-INF/MANIFEST.MF".equals(entryName) == false) {
                        aliases.add(new SourceAlias(
                        		jarName, entryName, source.getTimestamp()));
                    }
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    protected Set scanAll(SourceScanner scanner) {
        if(scanner == null) {
            throw new IllegalArgumentException();
        }
        Set aliases = new HashSet();
        for(Iterator it = scanner.scan(); it.hasNext(); ) {
            SourceDescriptor source = (SourceDescriptor)it.next();
            scanSource(source, aliases);
        }
        return aliases;
    }

    public Iterator scan() {
        String folder = _folderScanner.getFolder();
        Set aliases = (Set)_cache.get(folder);
        if(aliases == null) {
            aliases = scanAll(_folderScanner);
            _cache.put(folder, aliases);
        }
        return new MetaInfSourceIterator(aliases.iterator());
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
            SourceAlias alias = (SourceAlias)_it.next();
            ClassLoaderSourceDescriptor source =
                new ClassLoaderSourceDescriptor(null, alias.getSystemID(), null);
            source.setAttribute(SourceAlias.ALIAS, alias.getAlias());
            source.setTimestamp(alias.getTimestamp());
            return source;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}

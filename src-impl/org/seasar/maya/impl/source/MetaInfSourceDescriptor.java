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
package org.seasar.maya.impl.source;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.source.MetaInfCache.Entry;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.AbstractScanningIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * Jarファイルの/META-INFフォルダ中の相対パスで探すソースディスクリプタ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MetaInfSourceDescriptor 
		implements SourceDescriptor, CONST_IMPL {
    
	private static final long serialVersionUID = 8839712576913816936L;
	private static MetaInfCache _metaInfCache;

	protected static MetaInfCache getMetaInfCache() {
        if(_metaInfCache == null) {
            _metaInfCache = new MetaInfCache();
            _metaInfCache.init();
        }
        return _metaInfCache;
	}

    private String _systemID;
    private MetaInfCache.Entry _entry;
    
    public MetaInfSourceDescriptor(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _systemID = StringUtil.preparePath(systemID);
    }

    protected MetaInfSourceDescriptor(MetaInfCache.Entry entry) {
        if(entry == null) {
            throw new IllegalArgumentException();
        }
        _systemID = entry.getJarEntryName();
        _entry = entry;
    }
    
    public String getJarFileName() {
        if(exists()) {
            return _entry.getJarFileName();
        }
        return null;
    }
    
    public String getJarEntryName() {
        if(exists()) {
            return _entry.getJarEntryName();
        }
        return null;
    }
    
    public String getPath() {
        return PROTOCOL_META_INF + ":" + _systemID;
    }

    public String getProtocol() {
        return PROTOCOL_META_INF;
    }
    
    public String getSystemID() {
        return _systemID;
    }
    
    public boolean exists() {
        if(_entry == null) {
            _entry = getMetaInfCache().getMetaInfEntry(_systemID);
        }
        return _entry != null;
    }

    public InputStream getInputStream() {
        if (exists()) {
            return _entry.getInputStream();
        }
        return null;
    }

    public Date getTimestamp() {
        if(exists()) {
            return new Date(_entry.getTime());
        }
        return new Date(0);
    }

    public Iterator iterateChildren() {
        return new EntryToSourceIterator(getMetaInfCache().iterateEntry());
    }

    public Iterator iterateChildren(String extension) {
        return new EntryToSourceIterator( 
        	new EntryFilteredIterator(_systemID, extension, getMetaInfCache().iterateEntry()));
    }
    
    public String toString() {
        if(exists()) {
            return "[" + getJarFileName() + "]" + getJarEntryName();
        }
        return super.toString();
    }

    private class EntryToSourceIterator implements Iterator {
        
        private Iterator _iterator;
        
        private EntryToSourceIterator(Iterator iterator) {
            if(iterator == null) {
                throw new IllegalArgumentException();
            }
            _iterator = iterator;
        }
        
        public boolean hasNext() {
            return _iterator.hasNext();
        }

        public Object next() {
            Object ret = _iterator.next();
            if(ret instanceof MetaInfCache.Entry) {
                return new MetaInfSourceDescriptor((MetaInfCache.Entry)ret);
            }
            throw new IllegalStateException();
        }

        public void remove() {
            _iterator.remove();
        }

    }
    
    private class EntryFilteredIterator extends AbstractScanningIterator {
        
        private String _prefix;
        private String _extension;
        
        private EntryFilteredIterator(String prefix, String extension, Iterator it) {
            super(it);
            if(StringUtil.isEmpty(extension)) {
                throw new IllegalArgumentException();
            }
            _prefix = StringUtil.preparePath(prefix); 
            if(extension.startsWith(".") == false) {
            	extension = "." + extension;
            }
            _extension = extension.toLowerCase();
        }
        
        protected boolean filter(Object test) {
            if(test instanceof Entry) {
	            Entry entry = (Entry)test;
	            String name = entry.getJarEntryName();
	            if(StringUtil.hasValue(name)) {
	                String lowerName = name.toLowerCase();
	                return name.startsWith(_prefix) && lowerName.endsWith(_extension);
	            }
            }
            return false;
        }
        
    }
    
}

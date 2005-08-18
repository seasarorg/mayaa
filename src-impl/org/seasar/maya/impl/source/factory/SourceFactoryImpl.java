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
package org.seasar.maya.impl.source.factory;

import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.SourceScanner;
import org.seasar.maya.source.factory.DescriptorEntry;
import org.seasar.maya.source.factory.ScannerEntry;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * SourceDescriptorのファクトリ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceFactoryImpl
		implements SourceFactory, CONST_IMPL {
	
	private Map _descriptorEntries;
    private Map _scannerEntries;

	public SourceFactoryImpl() {
        _descriptorEntries = new HashMap();
        _scannerEntries = new HashMap();
	    prepareSourceFactory();
	}

//TODO maya.conf に記述 ++    
    protected void prepareSourceFactory() {
        _descriptorEntries.put(PROTOCOL_PAGE, new PageSourceEntry());
        _descriptorEntries.put(PROTOCOL_WEB_INF, new WebInfSourceEntry());
        _descriptorEntries.put(PROTOCOL_CONTEXT, new ContextSourceEntry());
        _descriptorEntries.put(PROTOCOL_JAVA, new JavaSourceEntry());
        _descriptorEntries.put(PROTOCOL_META_INF, new MetaInfSourceEntry());
    }
//--
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }
    
    protected String[] parseSourcePath(String path) {
        if(path == null) {
            throw new IllegalArgumentException();
        }
        String[] parsed = path.split(":");
        if(parsed.length == 2) {
            return parsed;
        } else if(parsed.length == 1) {
            return new String[] { PROTOCOL_CONTEXT, path };
        }
        throw new IllegalStateException();
    }

    public void putDescriptorEntry(DescriptorEntry entry) {
        if(entry == null) {
            throw new IllegalArgumentException();
        }
        _descriptorEntries.put(entry.getProtocol(), entry);
    }
    
    public SourceDescriptor createSourceDescriptor(String path) {
    	String[] parsed = parseSourcePath(path);
    	String protocol = parsed[0];
    	String systemID = parsed[1];
        DescriptorEntry entry = (DescriptorEntry)_descriptorEntries.get(protocol);
    	if(entry == null) {
    	    throw new IllegalStateException();
    	}
        return entry.createSourceDescriptor(systemID);
    }

    public void putScannerEntry(ScannerEntry entry) {
        if(entry == null) {
            throw new IllegalArgumentException();
        }
        _scannerEntries.put(entry.getProtocol(), entry);
    }

    public SourceScanner createSouceScanner(String path) {
        String[] parsed = parseSourcePath(path);
        String protocol = parsed[0];
        String systemID = parsed[1];
        ScannerEntry entry = (ScannerEntry)_scannerEntries.get(protocol);
        if(entry == null) {
            throw new IllegalStateException();
        }
        return entry.createSourceScanner(systemID);
    }
    
}

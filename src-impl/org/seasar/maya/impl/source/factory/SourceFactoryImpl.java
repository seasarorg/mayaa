/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceEntry;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * SourceDescriptorのファクトリ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceFactoryImpl
		implements SourceFactory, CONST_IMPL {
	
	private Map _sourceEntries;

	public SourceFactoryImpl() {
	    prepareSourceFactory();
	}
    
    protected void prepareSourceFactory() {
        _sourceEntries = new HashMap();
        _sourceEntries.put(PROTOCOL_PAGE, new PageSourceEntry());
        _sourceEntries.put(PROTOCOL_WEB_INF, new WebInfSourceEntry());
        _sourceEntries.put(PROTOCOL_CONTEXT, new ContextSourceEntry());
        _sourceEntries.put(PROTOCOL_JAVA, new JavaSourceEntry());
        _sourceEntries.put(PROTOCOL_META_INF, new MetaInfSourceEntry());
    }

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public Map getSourceEntries() {
        return _sourceEntries;
    }
    
    public void putSourceEntry(String protocol, SourceEntry entry) {
        if(StringUtil.isEmpty(protocol) || entry == null) {
            throw new IllegalArgumentException();
        }
        _sourceEntries.put(protocol, entry);
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
    
    public SourceDescriptor createSourceDescriptor(String path) {
    	String[] parsed = parseSourcePath(path);
    	String protocol = parsed[0];
    	String systemID = parsed[1];
    	SourceEntry entry = (SourceEntry)_sourceEntries.get(protocol);
    	if(entry == null) {
    	    throw new IllegalStateException();
    	}
        return entry.createSourceDescriptor(systemID);
    }
   
}

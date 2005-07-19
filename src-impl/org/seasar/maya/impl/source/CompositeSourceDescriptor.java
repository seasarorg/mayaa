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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeSourceDescriptor implements SourceDescriptor {

	private static final long serialVersionUID = -6124718310228340001L;
	private static final Date TIME_ZERO = new Date(0);
    private List _descriptors;
    private String _protocol;
    private String _systemID;
    
    public CompositeSourceDescriptor(String protocol, String systemID) {
        if(StringUtil.isEmpty(protocol) || StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _descriptors = new ArrayList();
        _protocol = protocol;
        _systemID = systemID;
    }

    public void add(SourceDescriptor descriptor) {
        if(descriptor == null) {
            throw new IllegalArgumentException();
        }
        synchronized(_descriptors) {
            _descriptors.add(descriptor);
        }
    }

    public String getPath() {
        return _protocol + ":" + _systemID;
    }

    public String getProtocol() {
        return _protocol;
    }

    public String getSystemID() {
        return _systemID;
    }
    
    public boolean exists() {
        for(int i = 0; i < _descriptors.size(); i++) {
            SourceDescriptor descriptor = (SourceDescriptor)_descriptors.get(i);
            if(descriptor.exists()) {
                return true;
            }
        }
        return false;
    }

    public InputStream getInputStream() {
        for(int i = 0; i < _descriptors.size(); i++) {
            SourceDescriptor descriptor = (SourceDescriptor)_descriptors.get(i);
            InputStream inputStream = descriptor.getInputStream();
            if(inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }
    
    public Date getTimestamp() {
        for(int i = 0; i < _descriptors.size(); i++) {
            SourceDescriptor descriptor = (SourceDescriptor)_descriptors.get(i);
            Date timestamp = descriptor.getTimestamp();
            if(timestamp.getTime() != TIME_ZERO.getTime()) {
                return timestamp;
            }
        }
        return TIME_ZERO;

    }

    public Iterator iterateChildren() {
        return NullIterator.getInstance();
    }
    
    public Iterator iterateChildren(String extension) {
        return NullIterator.getInstance();
    }

    public String toString() {
        if(exists()) {
            return getPath();
        }
        return super.toString();
    }
    
}

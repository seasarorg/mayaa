/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.source;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeSourceDescriptor extends ParameterAwareImpl
		implements SourceDescriptor {

    private static final long serialVersionUID = 7557914925525488748L;

    private List _descriptors = new ArrayList();
    private String _systemID;
    
    public void setSystemID(String systemID) {
        _systemID = StringUtil.preparePath(systemID);
    }

    public void addSourceDescriptor(SourceDescriptor source) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        synchronized(_descriptors) {
            _descriptors.add(source);
        }
    }
    
    public String getSystemID() {
        return _systemID;
    }
    
    private SourceDescriptor findDescriptor() {
        for(int i = 0; i < _descriptors.size(); i++) {
            SourceDescriptor descriptor = (SourceDescriptor)_descriptors.get(i);
            if(descriptor.exists()) {
                return descriptor;
            }
        }
        return null;
    }
    
    public boolean exists() {
        return findDescriptor() != null;
    }

    public InputStream getInputStream() {
        SourceDescriptor descriptor = findDescriptor();
        if(descriptor != null) {
            return descriptor.getInputStream();
        }
        return null;
    }
    
    public Date getTimestamp() {
        SourceDescriptor descriptor = findDescriptor();
        if(descriptor != null) {
            return descriptor.getTimestamp();
        }
        return new Date(0);

    }
    
}

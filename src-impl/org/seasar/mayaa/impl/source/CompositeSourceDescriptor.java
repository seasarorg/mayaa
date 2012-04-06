/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.source;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.source.WritableSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeSourceDescriptor extends ParameterAwareImpl
        implements WritableSourceDescriptor {

    private static final long serialVersionUID = 7557914925525488748L;

    private List _descriptors = new ArrayList();
    private SourceDescriptor _foundLast;

    public void addSourceDescriptor(SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_descriptors) {
            _descriptors.add(source);
        }
    }

    private SourceDescriptor findDescriptor() {
        if (_foundLast != null && _foundLast.exists()) {
            return _foundLast;
        }
        _foundLast = null;
        for (int i = 0; i < _descriptors.size(); i++) {
            SourceDescriptor descriptor = (SourceDescriptor) _descriptors.get(i);
            if (descriptor.exists()) {
                _foundLast = descriptor;
                return descriptor;
            }
        }
        return null;
    }

    public void setSystemID(String systemID) {
        _foundLast = null;
        _descriptors.clear();
        super.setSystemID(systemID);
    }

    public boolean exists() {
        return findDescriptor() != null;
    }

    public InputStream getInputStream() {
        SourceDescriptor descriptor = findDescriptor();
        if (descriptor != null) {
            return descriptor.getInputStream();
        }
        return null;
    }

    public Date getTimestamp() {
        SourceDescriptor descriptor = findDescriptor();
        if (descriptor != null) {
            return descriptor.getTimestamp();
        }
        return new Date(0);

    }

    /**
     * @see WritableSourceDescriptor#canWrite()
     */
    public boolean canWrite() {
        SourceDescriptor descriptor = findDescriptor();
        return (descriptor instanceof WritableSourceDescriptor) &&
                ((WritableSourceDescriptor) descriptor).canWrite();
    }

    /**
     * @see WritableSourceDescriptor#getOutputStream()
     */
    public OutputStream getOutputStream() {
        SourceDescriptor descriptor = findDescriptor();
        if (descriptor != null && (descriptor instanceof WritableSourceDescriptor)) {
            return ((WritableSourceDescriptor) descriptor).getOutputStream();
        }
        return null;
    }

}

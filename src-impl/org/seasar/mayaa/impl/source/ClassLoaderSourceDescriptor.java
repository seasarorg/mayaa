/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
import java.util.Date;

import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ClassLoaderSourceDescriptor extends ParameterAwareImpl
        implements SourceDescriptor {

    private static final long serialVersionUID = 1L;

    public static final String META_INF = "/META-INF";

    private String _root = "";
    private Class _neighbor;
    private transient InputStream _inputStream;
    private transient Date _timestamp;

    public void setNeighborClass(Class neighbor) {
        _neighbor = neighbor;
    }

    public Class getNeighborClass() {
        return _neighbor;
    }

    public void setRoot(String root) {
        _root = StringUtil.preparePath(root);
    }

    public String getRoot() {
        return _root;
    }

    public void setSystemID(String systemID) {
        if (systemID != null && systemID.indexOf(META_INF) != -1) {
            throw new ForbiddenPathException(systemID);
        }
        super.setSystemID(StringUtil.preparePath(systemID));
    }

    public boolean exists() {
        String path = (_root + getSystemID()).substring(1);
        if (_neighbor != null) {
            _inputStream = IOUtil.getResourceAsStream(path, _neighbor);
        }
        if (_inputStream == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            _inputStream = IOUtil.getResourceAsStream(path, loader);
        }
        return _inputStream != null;
    }

    public InputStream getInputStream() {
        if (exists()) {
            return _inputStream;
        }
        return null;
    }

    public void setTimestamp(Date timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException();
        }
        _timestamp = timestamp;
    }

    public Date getTimestamp() {
        if (_timestamp != null) {
            return _timestamp;
        }
        _timestamp = new Date();
        return _timestamp;
    }

}

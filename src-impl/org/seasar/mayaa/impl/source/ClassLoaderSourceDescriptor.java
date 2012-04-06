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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class ClassLoaderSourceDescriptor extends ParameterAwareImpl
        implements SourceDescriptor {

    private static final long serialVersionUID = 1L;

    public static final String META_INF = "/META-INF";
    private static final Date NOTFOUND_TIMESTAMP = new Date(0);

    private String _root = "";
    private Class _neighbor;
    private URL _url;
    private File _file;
    private Date _timestamp;

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
        if (_url == null) {
            synchronized (this) {
                if (_url == null) {
                    prepareURL();
                }
            }
        }
        return _url != null;
    }

    protected void prepareURL() {
        String path = (_root + getSystemID()).substring(1);
        if (_neighbor != null) {
            _url = IOUtil.getResource(path, _neighbor);
        }
        if (_url == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            _url = IOUtil.getResource(path, loader);
        }
        _file = IOUtil.getFile(_url);
        if (_timestamp == null && _file == null) {
            _timestamp = new Date();
        }
    }

    public InputStream getInputStream() {
        if (exists()) {
            return IOUtil.openStream(_url);
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
        if (exists() && _file != null && _file.exists()) {
            return new Date(_file.lastModified());
        }
        return NOTFOUND_TIMESTAMP;
    }

}

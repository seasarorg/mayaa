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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.cycle.scope.ApplicationScope;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationSourceDescriptor implements SourceDescriptor {

    public static final String WEB_INF = "/WEB-INF";

    private static final long serialVersionUID = -2775274363708858237L;

    private String _root = "";
    private String _systemID = "";

    private File _file;
    private ApplicationScope _application;
    private Map _attributes;

    // use while building ServiceProvider.
    public void setApplicationScope(ApplicationScope application) {
        if(application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }

    public ApplicationScope getApplicationScope() {
        if(_application == null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            _application = cycle.getApplicationScope();
        }
        return _application;
    }

    // use at InternalApplicationSourceScanner.FileToSourceIterator
    public void setFile(File file) {
        _file = file;
    }

    public File getFile() {
        exists();
        return _file;
    }

    public void setRoot(String root) {
        _root = StringUtil.preparePath(root);
    }

    public String getRoot() {
        return _root;
    }

    public void setSystemID(String systemID) {
        if(systemID != null && systemID.indexOf(WEB_INF) != -1) {
            throw new ForbiddenPathException(systemID);
        }
        _systemID = StringUtil.preparePath(systemID);
    }

    public String getSystemID() {
        return _systemID;
    }

    public boolean exists() {
        if(_file == null) {
            String realPath = getApplicationScope().getRealPath(_root + _systemID);
            if(StringUtil.hasValue(realPath)) {
                 File file = new File(realPath);
                 if(file.exists()) {
                     _file = file;
                 }
            }
        }
        return (_file != null) && _file.exists();
    }

    public InputStream getInputStream() {
        if(exists() && _file.isFile()) {
            try {
                return new FileInputStream(_file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public Date getTimestamp() {
        if(exists()) {
            return new Date(_file.lastModified());
        }
        return new Date(0);
    }

    public void setAttribute(String name, String value) {
        if(StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        if(_attributes == null) {
            _attributes = new HashMap();
        }
        _attributes.put(name, value);
    }

    public String getAttribute(String name) {
        if(_attributes == null) {
            return null;
        }
        return (String)_attributes.get(name);
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }

}

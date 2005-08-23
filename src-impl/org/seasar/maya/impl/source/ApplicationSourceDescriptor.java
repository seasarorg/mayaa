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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * WEBコンテキスト相対パスで探す、ソースディスクリプタ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ApplicationSourceDescriptor implements SourceDescriptor {

	private static final long serialVersionUID = -2775274363708858237L;

    private String _root;
    private String _systemID;
    private File _file;
    private Application _application;
    private Map _attributes;

    public ApplicationSourceDescriptor(String root, String systemID) {
        _root = StringUtil.preparePath(root);
        _systemID = StringUtil.preparePath(systemID);
    }
    
    // use while building ServiceProvider.
    public void setApplication(Application application) {
        if(application == null) {
            throw new IllegalArgumentException();
        }
        _application = application;
    }
    
    public Application getApplication() {
        if(_application == null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            _application = cycle.getApplication();
        }
        return _application;
    }

    public String getRoot() {
        return _root;
    }
    
    // use at InternalApplicationSourceScanner.FileToSourceIterator
    public void setFile(File file) {
        _file = file;
    }
    
    public File getFile() {
        exists();
        return _file;
    }
    
    public String getSystemID() {
        return _systemID;
    }
    
    public boolean exists() {
        if(_file == null) {
            String realPath = getApplication().getRealPath(_root + _systemID);
            if(StringUtil.hasValue(realPath)) {
                 File file = new File(realPath);
                 if(file.exists()) {
                     _file = file;
                 }
            }
        }
        return _file != null;
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

}

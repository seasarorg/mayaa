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
import java.io.InputStream;
import java.util.Date;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.FileUtil;
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

    public ApplicationSourceDescriptor(String root, String systemID) {
        this(root, systemID, null);
    }
    
    private ApplicationSourceDescriptor(String root, String systemID, File file) {
        _root = StringUtil.preparePath(root);
        _systemID = StringUtil.preparePath(systemID);
        _file = file;
    }
    
    public Application getApplication() {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        return cycle.getApplication();
    }

    public String getRoot() {
        return _root;
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
	        return FileUtil.getInputStream(_file);
        }
        return null;
    }

    public Date getTimestamp() {
        if(exists()) {
            return new Date(_file.lastModified());
        }
        return new Date(0);
    }

}

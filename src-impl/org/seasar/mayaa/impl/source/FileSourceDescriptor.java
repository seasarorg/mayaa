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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.source.WritableSourceDescriptor;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FileSourceDescriptor
        extends ParameterAwareImpl implements WritableSourceDescriptor, ChangeableRootSourceDescriptor {

    private static final long serialVersionUID = 9199082270985131347L;
    private static final Log LOG =
        LogFactory.getLog(FileSourceDescriptor.class);

    private static final Date NOTFOUND_TIMESTAMP = new Date(0);

    private String _root = "/";

    private File _file;

    // use at InternalApplicationSourceScanner.FileToSourceIterator
    public void setFile(File file) {
        _file = file;
    }

    public File getFile() {
        if (exists()) {
            return _file;
        }
        return null;
    }

    public boolean isDirectory() {
        if (exists()) {
            return _file.isDirectory();
        }
        return false;
    }

    public void setRoot(String root) {
        _root = StringUtil.preparePath(root);
    }

    public String getRoot() {
        return _root;
    }

    public void setSystemID(String systemID) {
        super.setSystemID(StringUtil.preparePath(systemID));
    }

    protected String getRealPath() {
        return _root + getSystemID();
    }

    protected void prepareFile() {
        if (_file == null) {
            String realPath = getRealPath();
            if (StringUtil.hasValue(realPath)) {
                 File file = new File(realPath);
                 if (IS_SECURE_WEB) {
                	 try {
                		 if (file.exists()) {
                			 _file = file;
                		 }
                	 } catch(AccessControlException e) {
                		 // OKな場所とそうでない場所があるのでtryはする。
                		 LOG.debug("access denied. " + file.toString());
                	 }
                 } else if (file.exists()) {
            		 _file = file;
                 }
            }
        }
    }

    public boolean exists() {
        prepareFile();
        return (_file != null) && _file.exists();
    }

    public InputStream getInputStream() {
        if (exists()) {
            if (_file.isFile()) {
                try {
                    return new FileInputStream(_file);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } else if (_file.isDirectory()) {
                CycleUtil.getServiceCycle().redirect(
                        _file.getName() + "/");
            }
        }
        return null;
    }

    public Date getTimestamp() {
        if (exists()) {
            return new Date(_file.lastModified());
        }
        return NOTFOUND_TIMESTAMP;
    }

    /**
     * @see WritableSourceDescriptor#canWrite()
     */
    public boolean canWrite() {
        prepareFile();
        return (_file != null) && _file.isFile() && _file.canWrite();
    }

    /**
     * @see WritableSourceDescriptor#getOutputStream()
     */
    public OutputStream getOutputStream() {
        if (canWrite()) {
            try {
                _file.mkdirs();
                return new FileOutputStream(_file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

}

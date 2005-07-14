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
package org.seasar.maya.impl.source;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.impl.util.FileUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * WEBコンテキスト相対パスで探す、ソースディスクリプタ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ServletSourceDescriptor implements SourceDescriptor {

	private static final long serialVersionUID = -2775274363708858237L;
	private Application _application;
    private String _protocol;
    private String _root;
    private String _systemID;
    private File _file;

    /**
     * @param webInf /WEB-INF相対パスで探すかどうか。
     * @param systemID ファイルのパス。
     */
    public ServletSourceDescriptor(
            Application application, String protocol, String root, String systemID) {
        this(application, protocol, root, systemID, null);
    }

    protected ServletSourceDescriptor(
            Application application, String protocol, String root, String systemID, File file) {
        if(application == null || StringUtil.isEmpty(protocol)) {
            throw new IllegalArgumentException();
        }
        _application = application;
        _protocol = protocol;
        _root = StringUtil.preparePath(root);
        _systemID = StringUtil.preparePath(systemID);
        _file = file;
    }
    
    /**
     * ServletContextを利用して、ファイルを探す。
     * @param path ファイルパス。
     */
    protected void prepareContextRelatedSource(String path) {
        if(_file == null) {
            String realPath = _application.getRealPath(path);
            if(StringUtil.hasValue(realPath)) {
                 File file = new File(realPath);
                 if(file.exists()) {
                     _file = file;
                 }
            }
        }
    }

    protected File getFile() {
        return _file;
    }

    public String getRoot() {
        return _root;
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
        prepareContextRelatedSource(_root + _systemID);
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

    public Iterator iterateChildren() {
    	if(exists() && _file.isDirectory()) {
    		return new FileToSourceIterator(FileUtil.iterateFiles(_file));
    	}
    	return NullIterator.getInstance();
    }
    
    public Iterator iterateChildren(String extension) {
    	if(exists() && _file.isDirectory()) {
    		return new FileToSourceIterator(FileUtil.iterateFiles(_file, extension));
    	}
    	return NullIterator.getInstance();
    }
    
    public String toString() {
        if(exists()) {
            return getPath();
        }
        return super.toString();
    }

    private class FileToSourceIterator implements Iterator {
        
        private Iterator _iterator;
        
        private FileToSourceIterator(Iterator iterator) {
            if(iterator == null) {
                throw new IllegalArgumentException();
            }
            _iterator = iterator;
        }
        
        public boolean hasNext() {
            return _iterator.hasNext();
        }

        private String getSystemID(File file) {
            String root = _application.getRealPath(_root);
            String absolutePath = file.getAbsolutePath();
            return absolutePath.substring(root.length());
        }
        
        public Object next() {
            Object ret = _iterator.next();
            if(ret instanceof File) {
                File file = (File)ret;
                String systemID = getSystemID(file);
                return new ServletSourceDescriptor(_application, _protocol, _root, systemID, file);
            }
            throw new IllegalStateException();
        }

        public void remove() {
            _iterator.remove();
        }

    }

}

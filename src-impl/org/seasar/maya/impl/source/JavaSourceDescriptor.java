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

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.collection.NullIterator;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class JavaSourceDescriptor implements SourceDescriptor, CONST_IMPL {
    
	private static final long serialVersionUID = -4924522601395047024L;

	private Class _neighbor;
    private String _className;
    private InputStream _inputStream;

    public JavaSourceDescriptor(String systemID, Class neighbor) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _neighbor = neighbor;
        if(systemID.startsWith("/")) {
            systemID = systemID.substring(1);
        }
        _className = systemID;
    }
    
    public JavaSourceDescriptor(String systemID) {
        this(systemID, null);
    }

    protected void prepareLoadedSource() {
        if(_neighbor != null) {
            _inputStream = _neighbor.getResourceAsStream(_className);
        } else {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            _inputStream = loader.getResourceAsStream(_className);
        }
    }
    
    public String getPath() {
        return  PROTOCOL_JAVA + ":/" + _className;
    }

    public String getProtocol() {
        return PROTOCOL_JAVA;
    }

    public String getSystemID() {
        return "/" + _className;
    }
    
    public boolean exists() {
        prepareLoadedSource();
        return _inputStream != null;
    }

    public InputStream getInputStream() {
        if(exists()) {
            return _inputStream;
        }
        return null;
    }
    
    public Date getTimestamp() {
        return new Date(0);
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

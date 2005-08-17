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

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DelaySourceDescriptor extends NullSourceDescriptor {

	private static final long serialVersionUID = 8798364851381291687L;

	private String _path;
    private SourceDescriptor _source;
    
    public DelaySourceDescriptor(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        _path = path;
    }
    
    public boolean exists() {
        if(_source == null) {
            SourceFactory factory = 
                ProviderFactory.getServiceProvider().getSourceFactory();
            _source = factory.createSourceDescriptor(_path);
        }
        return _source.exists();
    }

    public InputStream getInputStream() {
        if(exists()) {
            return _source.getInputStream();
        }
        return super.getInputStream();
    }

    public String getPath() {
        return _path;
    }
    
    public String getProtocol() {
        if(exists()) {
            return _source.getProtocol();
        }
        return super.getProtocol();
    }
    
    public String getSystemID() {
        if(exists()) {
            return _source.getSystemID();
        }
        return super.getSystemID();
    }
    
    public Date getTimestamp() {
        if(exists()) {
            return _source.getTimestamp();
        }
        return super.getTimestamp();
    }

    public Iterator iterateChildren() {
        return super.iterateChildren();
    }
    
    public Iterator iterateChildren(String extension) {
        return super.iterateChildren(extension);
    }

    public String toString() {
        if(exists()) {
            return _source.toString();
        }
        return super.toString();
    }
    
}

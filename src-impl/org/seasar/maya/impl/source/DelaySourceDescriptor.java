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

import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ProviderFactory;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DelaySourceDescriptor implements SourceDescriptor {

    private static final long serialVersionUID = 1596798824321986307L;
    
    private String _systemID;
    private SourceDescriptor _source;

    public void setSystemID(String systemID) {
        if(StringUtil.isEmpty(systemID)) {
            throw new IllegalArgumentException();
        }
        _systemID = systemID;
    }
    
    public String getSystemID() {
        return _systemID;
    }
    
    public boolean exists() {
        if(_source == null) {
            ServiceProvider provider = ProviderFactory.getServiceProvider();
            _source = provider.getPageSourceDescriptor(_systemID);
        }
        return _source.exists();
    }

    public InputStream getInputStream() {
        if(exists()) {
            return _source.getInputStream();
        }
        return null;
    }
    
    public Date getTimestamp() {
        if(exists()) {
            return _source.getTimestamp();
        }
        return new Date(0);
    }

    public String getAttribute(String name) {
        if(exists()) {
            return _source.getAttribute(name);
        }
        return null;
    }

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(name);
    }
    
}

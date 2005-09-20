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
package org.seasar.maya.impl.cycle;

import org.seasar.maya.cycle.Application;
import org.seasar.maya.cycle.Request;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.EngineUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.factory.ProviderFactory;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractRequest
        extends AbstractWritableAttributeScope
        implements Request, CONST_IMPL {

    private String _pageName;
    private String _requestedSuffix;
    private String _extension;
    private String _mimeType;

    public void parsePath(String path) {
        String suffixSeparator = EngineUtil.getEngineSetting(
                SUFFIX_SEPARATOR, "$");
        String[] parsed = StringUtil.parsePath(path, suffixSeparator);
        _pageName = parsed[0];
        _requestedSuffix = parsed[1];
        _extension = parsed[2];
        Application application = 
            ProviderFactory.getServiceProvider().getApplication();
        _mimeType = application.getMimeType(path);
    }
    
    public void setForwardPath(String relativeUrlPath) {
        if(StringUtil.isEmpty(relativeUrlPath)) {
            throw new IllegalArgumentException();
        }
        parsePath(relativeUrlPath);
    }

    public String getScopeName() {
        return ServiceCycle.SCOPE_REQUEST;
    }

    public String getPageName() {
        if(_pageName == null) {
            parsePath(getRequestedPath());
        }
        return _pageName;
    }

    public String getRequestedSuffix() {
        if(_requestedSuffix == null) {
            parsePath(getRequestedPath());
        }
        return _requestedSuffix;
    }

    public String getExtension() {
        if(_extension == null) {
            parsePath(getRequestedPath());
        }
        return _extension;
    }
    
    public String getMimeType() {
        if(_mimeType == null) {
            parsePath(getRequestedPath());
        }
        return _mimeType;
    }
    
}

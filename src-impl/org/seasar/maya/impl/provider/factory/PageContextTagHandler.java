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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.impl.provider.PageContextSettingImpl;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class PageContextTagHandler extends TagHandler {
    
    private EngineTagHandler _parent;
    
    public PageContextTagHandler(EngineTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
    }

    protected void start(Attributes attributes) {
        PageContextSettingImpl setting = new PageContextSettingImpl();
        String errorPageURL = XmlUtil.getStringValue(attributes, "errorPageURL", null);
        int bufferSize = XmlUtil.getIntValue(attributes, "bufferSize", 8 * 1024);
        boolean needSession = XmlUtil.getBooleanValue(attributes, "needSession", true);
        boolean autoFlush = XmlUtil.getBooleanValue(attributes, "needSession", true);
        setting.setErrorPageURL(errorPageURL);
        setting.setBufferSize(bufferSize);
        setting.setNeedSession(needSession);
        setting.setAutoFush(autoFlush);
        _parent.getEngineSetting().setPageContextSetting(setting);
    }
    
}

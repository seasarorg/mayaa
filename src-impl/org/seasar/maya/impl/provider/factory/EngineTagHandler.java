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

import org.seasar.maya.impl.engine.EngineImpl;
import org.seasar.maya.impl.provider.EngineSettingImpl;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EngineTagHandler extends AbstractParameterizableTagHandler {
    
    private ServiceTagHandler _parent;
    private EngineImpl _engine;
    private EngineSettingImpl _engineSetting;
    
    public EngineTagHandler(ServiceTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler("specification", new SpecificationTagHandler(this));
        putHandler("pageContext", new PageContextTagHandler(this));
        putHandler("errorHandler", new ErrorHandlerTagHandler(this));
    }

    private EngineSettingImpl createEngineSetting(Attributes attributes) {
        EngineSettingImpl engineSetting = new EngineSettingImpl();
        boolean checkTimestamp = XmlUtil.getBooleanValue(attributes, "checkTimestamp", true);
        boolean outputWhitespace = XmlUtil.getBooleanValue(attributes, "outputWhitespace", true);
        String suffixSeparator = XmlUtil.getStringValue(attributes, "suffixSeparator", "$");
        boolean reportUnresolvedID = XmlUtil.getBooleanValue(attributes, "reportUnresolvedID", true);
        engineSetting.setCheckTimestamp(checkTimestamp);
        engineSetting.setOutputWhitespace(outputWhitespace);
        engineSetting.setSuffixSeparator(suffixSeparator);
        engineSetting.setReportUnresolvedID(reportUnresolvedID);
        return engineSetting;
    }
    
    protected void start(Attributes attributes) {
        _engine = new EngineImpl();
        _engineSetting = createEngineSetting(attributes);
        _engine.setEngineSetting(_engineSetting);
        _parent.getServiceProvider().setEngine(_engine);
    }
    
    protected void end(String body) {
        _engineSetting = null;
        _engine = null;
    }
    
    public EngineSettingImpl getEngineSetting() {
        if(_engineSetting == null) {
            throw new IllegalStateException();
        }
        return _engineSetting;
    }
    
    public EngineImpl getEngine() {
        if(_engine == null) {
            throw new IllegalStateException();
        }
        return _engine;
    }

    public Parameterizable getParameterizable() {
        return getEngine();
    }
    
}

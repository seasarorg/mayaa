/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.processor.ProcessStatus;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.cycle.CycleUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WriteProcessor extends TemplateProcessorSupport {

	private static final long serialVersionUID = -8069702863937020350L;

    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private ProcessorProperty _escapeXml;
    private ProcessorProperty _escapeWhitespace;

    // MLD property, expectedClass=java.lang.String
    public void setValue(ProcessorProperty value) {
        _value = value;
    }

    public void setDefault(ProcessorProperty defaultValue) {
    	_default = defaultValue;
    }

    public void setEscapeXml(ProcessorProperty escapeXml) {
        _escapeXml = escapeXml;
    }

    public void setEscapeWhitespace(ProcessorProperty escapeWhitespace) {
        _escapeWhitespace = escapeWhitespace;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_value != null) {
            String ret = (String)_value.getValue().execute(null);
            if(StringUtil.isEmpty(ret) && _default != null) {
                ret = (String)_default.getValue().execute(null);
            }
            if (_escapeXml != null) {
                boolean escapeXml = ObjectUtil.booleanValue(
                        _escapeXml.getValue().execute(null), false);
                if (escapeXml) {
                    ret = StringUtil.escapeXml(ret);
                }
            }
            if (_escapeWhitespace != null) {
                boolean escapeWhitespace = ObjectUtil.booleanValue(
                        _escapeWhitespace.getValue().execute(null), false);
                if (escapeWhitespace) {
                    ret = StringUtil.escapeWhitespace(ret);
                }
            }
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(ret);
        }
        return ProcessStatus.SKIP_BODY;
    }

}

/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WriteProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -8069702863937020350L;

    private boolean _forHTML;
    private boolean _forHyperText;
    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private ProcessorProperty _escapeXml;
    private ProcessorProperty _escapeWhitespace;
    private ProcessorProperty _escapeEol;

    public void initialize() {
        QName originalQName = getOriginalNode().getQName();
        _forHTML = isHTML(originalQName);
        _forHyperText = _forHTML || isXHTML(originalQName);
    }

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

    public void setEscapeEol(ProcessorProperty escapeEol) {
        _escapeEol = escapeEol;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if(_value != null) {
            String ret = (String)_value.getValue().execute(null);
            if(StringUtil.isEmpty(ret) && _default != null) {
                ret = (String)_default.getValue().execute(null);
            }
            if (toBoolean(_escapeXml)) {
                ret = StringUtil.escapeXml(ret);
            }
            if (_forHyperText && toBoolean(_escapeEol)) {
                ret = StringUtil.escapeEol(ret, _forHTML);
            }
            if (toBoolean(_escapeWhitespace)) {
                ret = StringUtil.escapeWhitespace(ret);
            }
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(ret);
        }
        return ProcessStatus.SKIP_BODY;
    }

    private boolean toBoolean(ProcessorProperty property) {
        return property != null
            && ObjectUtil.booleanValue(property.getValue().execute(null), false);
    }

}

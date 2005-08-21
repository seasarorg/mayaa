/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;

/**
 * JSTL の fmt:formatDate にあたるネイティブプロセッサ.
 * @author suga
 */
public class FormatDateProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -9118340781420704614L;

    /* rtexprvalue */
    private ProcessorProperty _value;

    /* not rtexprvalue */
    private String _var;
    private String _scope;

    private String _type;
    private String _pattern;
    private String _timeZone;
    private String _dateStyle;
    private String _timeStyle;

    
    public ProcessStatus doStartProcess() {
        Date value = getDateValue();
        if (value == null) {
            return EVAL_PAGE;
        }

        Locale locale = FormatUtil.getLocale();

        String formatted = null;
        if (locale == null) {
            formatted = value.toString();
        } else {
            DateFormat format = FormatUtil.createFormat(
            			locale,_type,_pattern,_dateStyle,_timeStyle);
    
            TimeZone timeZone = FormatUtil.parseTimeZone(_timeZone);
            if (timeZone != null) {
                format.setTimeZone(timeZone);
            }
    
            formatted = format.format(value);
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (_var != null) {
            AttributeScope scope = cycle.getAttributeScope(_scope);
            scope.setAttribute(_var, formatted);
        } else {
            cycle.getResponse().write(formatted);
        }

        return SKIP_BODY;
    }

    /**
     * value を評価して Date を取得する。
     * @return Date
     * @throws IllegalTagAttributeException dateが不正な場合
     */
    protected Date getDateValue() {
        Object value = null;
        value = _value.getValue();
        if (value == null) {
            return null;
        } else if (value instanceof Date) {
            return (Date) value;
        }
        throw new IllegalTagAttributeException(
                getTemplate(), "formatDate", "value", _value, null);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName());
        sb.append(": value=");
        sb.append(_value);
        sb.append(", type=");
        sb.append(_type);
        sb.append(", pattern=");
        sb.append(_pattern);
        sb.append(", timeZone=");
        sb.append(_timeZone);
        sb.append(", dateStyle=");
        sb.append(_dateStyle);
        sb.append(", timeStyle=");
        sb.append(_timeStyle);
        sb.append(", var=");
        sb.append(_var);
        return new String(sb);
    }

    
    // setter
    public void setValue(ProcessorProperty value) {
        if(value == null) {
            throw new IllegalArgumentException();
        }
        _value = value;
    }

    public void setType(String type) {
        _type = type;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
    }

    public void setTimeZone(String timeZone) {
        _timeZone = timeZone;
    }

    public void setDateStyle(String dateStyle) {
        _dateStyle = dateStyle;
    }

    public void setTimeStyle(String timeStyle) {
        _timeStyle = timeStyle;
    }

    public void setVar(String var) {
        _var = var;
    }

    public void setScope(String scope) {
        _scope = scope;
    }
}

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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

public class ParseNumberProcessor extends AbstractBodyProcessor {
    
    private static final long serialVersionUID = 7758056446750833308L;

    private ProcessorProperty _type ;
    private ProcessorProperty _parseLocale ;
    private ProcessorProperty _integerOnly ;
    private ProcessorProperty _pattern ;
    private ProcessorProperty _var ;
    private ProcessorProperty _scope ;
    
    public void setIntegerOnly(ProcessorProperty integerOnly) {
        _integerOnly = integerOnly;
    }

    public void setParseLocale(ProcessorProperty parseLocale) {
        _parseLocale = parseLocale;
    }

    public void setPattern(ProcessorProperty pattern) {
        _pattern = pattern;
    }

    public void setScope(ProcessorProperty scope) {
        _scope = scope;
    }

    public void setType(ProcessorProperty type) {
        _type = type;
    }

    public void setVar(ProcessorProperty var) {
        _var = var;
    }

    public ProcessStatus process(ServiceCycle cycle, Object obj) {
        Locale       locale   = getLocale(cycle);
        NumberFormat format   = getFormat(locale);
        Number       number   = null;
        boolean isIntegerOnly = ObjectUtil.booleanValue(
                                _integerOnly.getLiteral(),false);

        try {
            number = format.parse(obj.toString());
            if( isIntegerOnly ){
                number = new Integer( number.intValue() );
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if(StringUtil.isEmpty(_var.getLiteral())){
            cycle.getResponse().write(number.toString());
        } else {
            AttributeValue attributeValue 
                    = AttributeValueFactory.create(
                            _var.getLiteral(),_scope.getLiteral());
            attributeValue.setValue(cycle,number);
        }
        return SKIP_BODY;
    }

    private NumberFormat getFormat(Locale locale) {
        if( StringUtil.isEmpty(_pattern.getLiteral()) == false )
            return new DecimalFormat(_pattern.getLiteral());
        if("NUMBER".equals(_type.getLiteral()))
            return NumberFormat.getNumberInstance(locale);
        if("CURRENCY".equals(_type.getLiteral()))
            return NumberFormat.getCurrencyInstance(locale);
        if("PERCENT".equals(_type.getLiteral()))
            return NumberFormat.getPercentInstance(locale);        
        return NumberFormat.getInstance(locale);
    }

    private Locale getLocale(ServiceCycle cycle) {
        Locale locale       = Locale.getDefault();
        Object parseLocale  = _parseLocale.getValue(cycle);
        if( parseLocale instanceof Locale ){
            locale = (Locale)parseLocale ;
        }
        return locale;
    }
    
}

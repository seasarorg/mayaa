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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.seasar.maya.engine.processor.NullProcessorProperty;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

public class ParseNumberProcessor extends AbstractBodyProcessor {
    
    private static final long serialVersionUID = 7758056446750833308L;

    private ProcessorProperty _parseLocale = NullProcessorProperty.NULL;
    
    private String _var ;
    private String _scope ;
    private String _integerOnly ;
    private String _type ;
    private String _pattern ;

    public ProcessStatus process(Object obj) {
        Locale locale = getLocale();
        NumberFormat format = FormatUtil.getFormat(locale,_pattern,_type);
        Number number = null;
        boolean isIntegerOnly = ObjectUtil.booleanValue(_integerOnly,false);
        try {
            number = format.parse(obj.toString());
            if( isIntegerOnly ){
                number = new Integer( number.intValue() );
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        
        AttributeValue attributeValue = AttributeValueFactory.createForceOutputer(_var,_scope);
        attributeValue.setValue(number);
        
        return SKIP_BODY;
    }


    private Locale getLocale() {
        Locale locale = Locale.getDefault();
        Object parseLocale  = _parseLocale.getValue();
        if(parseLocale instanceof Locale){
            return (Locale)parseLocale;
	    } else if (parseLocale instanceof String) {
	        return FormatUtil.parseLocale((String)parseLocale);
	    }
        return locale;
    }

    
    // setter
    public void setValue(ProcessorProperty value) {
    	super.setValue(value);
    }

    public void setVar(String var) {
        _var = var;
    }

    public void setIntegerOnly(String integerOnly) {
        _integerOnly = integerOnly;
    }

    public void setParseLocale(ProcessorProperty parseLocale) {
        _parseLocale = parseLocale;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
    }

    public void setScope(String scope) {
        _scope = scope;
    }

    public void setType(String type) {
        _type = type;
    }
}

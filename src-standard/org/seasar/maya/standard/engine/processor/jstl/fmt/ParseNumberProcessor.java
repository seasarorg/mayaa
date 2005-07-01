package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.AttributeValue;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

public class ParseNumberProcessor extends AbstractBodyProcessor {
    private ProcessorProperty _value ;
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

    public void setValue(ProcessorProperty value) {
        _value = value;
    }

    public void setVar(ProcessorProperty var) {
        _var = var;
    }

    public int process(PageContext context, Object obj) {
        Locale       locale   = getLocale(context);
        NumberFormat format   = getFormat(locale);
        Number       number   = null;
        boolean isIntegerOnly = ObjectUtil.booleanValue(_integerOnly.getLiteral(),false);
        
        try {
            number = format.parse(obj.toString());
            if( isIntegerOnly ){
                number = new Integer( number.intValue() );
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if(StringUtil.isEmpty(_var.getLiteral())){
            Writer out = context.getOut();
            try {
                out.write(number.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            AttributeValue attributeValue = AttributeValueFactory.create(_var.getLiteral(),_scope.getLiteral());
            attributeValue.setValue(context,number);
        }
        return Tag.SKIP_BODY;
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

    private Locale getLocale(PageContext context) {
        Locale locale       = Locale.getDefault();
        Object parseLocale  = _parseLocale.getValue(context);
        if( parseLocale instanceof Locale ){
            locale = (Locale)parseLocale ;
        }
        return locale;
    }
}

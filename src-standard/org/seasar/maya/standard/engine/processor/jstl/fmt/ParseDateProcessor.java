package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.engine.processor.TemplateProcessorSupport;
import org.seasar.maya.engine.processor.TemplateProcessor.ProcessStatus;
import org.seasar.maya.standard.engine.processor.AttributeValueFactory;

/**
 * 
 * @author maruo_syunsuke
 */
public class ParseDateProcessor extends TemplateProcessorSupport {

	private ProcessorProperty _value;

    private String _type;
    private String _dateStyle;
    private String _timeStyle;
    private String _pattern;
    private String _parseLocale ;
    private String _timeZone;
    private String _var;
    private String _scope;

    public ProcessStatus doStartProcess() {
		try {
			DateFormat dateFormat = FormatUtil.createFormat(
					FormatUtil.parseLocale(_parseLocale),
					_type,_pattern,_dateStyle,_timeStyle);
			dateFormat.setTimeZone(FormatUtil.parseTimeZone(_timeZone));
			
	    	Date date = dateFormat.parse((String)_value.getValue());
			AttributeValueFactory.createForceOutputer(_var,_scope).setValue(date);		
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		return EVAL_PAGE;
	}

    
    // setter
	public void setDateStyle(String dateStyle) {
		_dateStyle = dateStyle;
	}

	public void setParseLocale(String parseLocale) {
		_parseLocale = parseLocale;
	}

	public void setPattern(String pattern) {
		_pattern = pattern;
	}

	public void setScope(String scope) {
		_scope = scope;
	}

	public void setTimeStyle(String timeStyle) {
		_timeStyle = timeStyle;
	}

	public void setTimeZone(String timeZone) {
		_timeZone = timeZone;
	}

	public void setType(String type) {
		_type = type;
	}

	public void setValue(ProcessorProperty value) {
		_value = value;
	}

	public void setVar(String var) {
		_var = var;
	}
}

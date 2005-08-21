package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.util.TimeZone;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author maruo_syunsuke
 */
public class TimeZoneProcessor extends TemplateProcessorSupport {
	private String _value ;
	
	public ProcessStatus doStartProcess() {
		TimeZone timeZone = FormatUtil.parseTimeZone(_value);
    	FormatUtil.setTimeZone(timeZone);
		return EVAL_PAGE;
	}

	public ProcessStatus doEndProcess() {
    	FormatUtil.removeTimeZone();
		return super.doEndProcess();
	}
	
	public void setValue(String value) {
		_value = value;
	}
}

package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.util.Locale;

import org.seasar.maya.engine.processor.TemplateProcessorSupport;

/**
 * @author maruo_syunsuke
 */
public class SetLocaleProcessor extends TemplateProcessorSupport {
	private String _value ;
	private String _valiant ;
	private String _scope ;
	
	public ProcessStatus doStartProcess() {
    	Locale locale = FormatUtil.parseLocale(_value,_valiant);
    	FormatUtil.setLocale(locale,_scope);
		return EVAL_PAGE;
	}

    public void setValiant(String valiant) {
		_valiant = valiant;
	}

	public void setValue(String value) {
		_value = value;
	}
	public void setScope(String scope) {
		_scope = scope;
	}
}

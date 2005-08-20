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

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.seasar.maya.cycle.AttributeScope;
import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.processor.ProcessorProperty;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.standard.engine.processor.AbstractBodyProcessor;
import org.seasar.maya.standard.engine.processor.util.ProcessorPropertyUtil;

/**
 * JSTL の fmt:formatNumber にあたるネイティブプロセッサ.
 * 
 * @author duran
 *  
 */
public class FormatNumberProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = 2352354341113789083L;
    private static final String NUMBER   = "number";
    private static final String CURRENCY = "currency";
    private static final String PERCENT  = "percent";
    
    private ProcessorProperty _typeAttr;
    private ProcessorProperty _patternAttr;
    private ProcessorProperty _currencyCodeAttr;
    private ProcessorProperty _currencySymbolAttr;
    private ProcessorProperty _groupingUsedAttr;
    
    private ProcessorProperty _maxIntegerDigitsAttr;
    private ProcessorProperty _minIntegerDigitsAttr;
    private ProcessorProperty _maxFractionDigitsAttr;
    private ProcessorProperty _minFractionDigitsAttr;

    private String _type;
    private String _pattern;
    private String _currencyCode;
    private String _currencySymbol;
    private boolean	_groupingUsed = true;

    private String _var;
    private String _scope;

    private static final String FMT_LOCALE = "javax.servlet.jsp.jstl.fmt.locale";

    private static final Class[] GET_INSTANCE_PARAM_TYPES = new Class[] { String.class };
    private static Class currencyClass;

    static {
        try {
            //J2SEのバージョンが1.4以降の場合
            currencyClass = Class.forName("java.util.Currency");
        } catch (Exception cnfe) {
        }
    }

    private void initParameter() {
        _type           = ProcessorPropertyUtil.getString(_typeAttr);
        _pattern        = ProcessorPropertyUtil.getString(_patternAttr);
        _currencyCode   = ProcessorPropertyUtil.getString(_currencyCodeAttr);
        _currencySymbol = ProcessorPropertyUtil.getString(_currencySymbolAttr);

        _groupingUsed = true;
        if(_groupingUsedAttr != null){
            Object obj = _groupingUsedAttr.getValue();
        	if(obj instanceof Boolean) {
        	    _groupingUsed = ((Boolean)obj).booleanValue();
        	} else if(obj instanceof String) {
        	    _groupingUsed = Boolean.valueOf((String)obj).booleanValue();
        	}
        }
    }
    
    protected ProcessStatus process(Object obj) {

        if (obj instanceof String && obj.equals("")){
        	return EVAL_PAGE;
        }

        initParameter();

        String formatted   = getFormatedString(obj);
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        if (_var != null) {
            AttributeScope scope = cycle.getAttributeScope(_scope);
            scope.setAttribute(_var, formatted);
        } else {
            cycle.getResponse().write(formatted);
        }
        return EVAL_PAGE;
    }

	private String getFormatedString(Object obj) {
        Number input  = createNumberObject(obj);
        Locale locale = getLocale();
		if (locale == null) return input.toString(); 

		// Create formatter
        NumberFormat formatter = null;
        if(StringUtil.isEmpty(_pattern) == false) {
            // if 'pattern' is specified, 'type' is ignored
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            formatter = new DecimalFormat(_pattern, symbols);
        } else {
            formatter = createFormatter(locale);
        }
        if(StringUtil.isEmpty(_pattern) == false
                || CURRENCY.equalsIgnoreCase(_type)) {
            try {
                setCurrency(formatter);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        configureFormatter(formatter);
        return formatter.format(input);
	}

	private Number createNumberObject(Object obj) {
        if (obj instanceof String) {
            try {
            	if (((String) obj).indexOf('.') != -1) {
                    return Double.valueOf((String)obj);
                } else {
                    return Long.valueOf(  (String)obj);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalTagAttributeException(getTemplate(),
                		"formatNumber", "value", obj, null);
            }
        } else if(obj instanceof Number){
        	return (Number)obj;
        } else {
            throw new IllegalTagAttributeException(getTemplate(),
                    "formatNumber", "value", obj, null);
        }
	}
 
    /**
     * NumberFormatを作成します。<br>
     * typeがセットされていなければ<br>
     * typeは "number" "currency" "persent"<br>
     * typeの指定がなければ "number"
     * @param locale
     * @return NumberFormat
     */
    private NumberFormat createFormatter(Locale locale) {

        NumberFormat formatter = null;

        if ((_type == null) || NUMBER.equalsIgnoreCase(_type)) {
            formatter = NumberFormat.getNumberInstance(locale);
        } else if (CURRENCY.equalsIgnoreCase(_type)) {
        	formatter = NumberFormat.getCurrencyInstance(locale);
        } else if (PERCENT.equalsIgnoreCase(_type)) {
            formatter = NumberFormat.getPercentInstance(locale);
        } else {
            throw new IllegalTagAttributeException(getTemplate(),
                    "formatNumber", "type", _type, null);
        }

        return formatter;

    }

    /**
     * フォーマットを設定します。
     * 
     * @param formatter
     */
    private void configureFormatter(NumberFormat formatter) {
        formatter.setGroupingUsed(_groupingUsed);
        Integer maxIntegerDigits = ProcessorPropertyUtil.getInteger(_maxIntegerDigitsAttr);
		if (maxIntegerDigits != null) {
            formatter.setMaximumIntegerDigits(maxIntegerDigits.intValue());
        }
        Integer minIntegerDigits = ProcessorPropertyUtil.getInteger(_minIntegerDigitsAttr);
		if (minIntegerDigits != null) {
            formatter.setMinimumIntegerDigits(minIntegerDigits.intValue());
        }
        Integer maxFractionDigits = ProcessorPropertyUtil.getInteger(_maxFractionDigitsAttr);
		if (maxFractionDigits != null) {
            formatter.setMaximumFractionDigits(maxFractionDigits.intValue());
        }
        Integer minFractionDigits = ProcessorPropertyUtil.getInteger(_minFractionDigitsAttr);
		if (minFractionDigits != null) {
            formatter.setMinimumFractionDigits(minFractionDigits.intValue());
        }
    }

    /**
     * Locale を取得する。 ページコンテキスト内の FMT_LOCALE を探し、あればそれを返す。 無ければデフォルト Locale を返す。
     * @return Locale
     */
    protected Locale getLocale() {
        Object locale = CycleUtil.findAttribute(FMT_LOCALE);

        if (locale instanceof Locale) {
            return (Locale) locale;
        } else if (locale instanceof String) {
            return parseLocale((String) locale);
        } else {
            return Locale.getDefault();
        }
    }

    /**
     * Locale文字列をパースし、Localeオブジェクトを返す。
     * 
     * @param localeString
     *            Locale文字列
     * @return LocaleObject
     * @throws IllegalArgumentException
     *             形式不正の場合
     */
    protected Locale parseLocale(String localeString) {
        int index = -1;
        String language;
        String country;

        if (( index = localeString.indexOf('-')) >= 0 ||
            ( index = localeString.indexOf('_')) >= 0 ){
            language = localeString.substring(0, index);
            country  = localeString.substring(index+1);
        } else {
            language = localeString;
            country = null;
        }

        if (StringUtil.isEmpty(language)
                || (country != null && country.length() == 0)) {
            throw new IllegalArgumentException("invalid Locale: " + localeString);
        }

        if (country == null) {
            country = "";
        }

        return new Locale(language, country);
    }

    /**
     * 通貨値を設定します。 <br>
     * J2SE1.4以降の場合はjava.util.Currencyを用います。 <br>
     * 1.4以前の場合はDecimalFormatterを用います。
     * 
     * @param formatter
     * @throws Exception
     */
    private void setCurrency(NumberFormat formatter) throws Exception {

        String code   = _currencyCode;
        String symbol = _currencySymbol;

        if (currencyClass != null && code != null) {
            /*
			 * java.util.Currency.getInstance()
			 */
            Method methodGetInstance = currencyClass.getMethod(
            								"getInstance",
            								GET_INSTANCE_PARAM_TYPES);
            Object currency          = methodGetInstance.invoke(
            								null, 
            								new Object[]{code});

            /*
             * java.text.NumberFormat.setCurrency()
             */
            Class  numberFormatClass = Class.forName(
            								"java.text.NumberFormat");
            Method methodSetCurrency = numberFormatClass.getMethod(
            								"setCurrency", 
            								new Class[]{currencyClass});
            methodSetCurrency.invoke(
            								formatter, 
            								new Object[]{currency});
        } else if( symbol != null ){
            /*
             * Let potential ClassCastException propagate up (will almost never
             * happen)
             */
            DecimalFormat        df  = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setCurrencySymbol(symbol);
            df.setDecimalFormatSymbols(dfs);
        }
    }

    public boolean isChildEvaluation() {
        return true;
    }
    
    // setter 
    public void setValue(ProcessorProperty value) {
    	super.setValue(value);
    }
    
    public void setType(ProcessorProperty type) {
    	_typeAttr = type;
    }
    
    public void setPattern(ProcessorProperty pattern) {
    	_patternAttr = pattern;
    }
    
    public void setCurrencyCode(ProcessorProperty currencyCode) {
    	_currencyCodeAttr = currencyCode;
    }

    public void setCurrencySymbol(ProcessorProperty currencySymbol) {
    	_currencySymbolAttr = currencySymbol;
    }
    
    public void setGroupingUsed(ProcessorProperty groupingUsed) {
    	_groupingUsedAttr = groupingUsed;
    }

    public void setMaxIntegerDigits(ProcessorProperty maxIntegerDigits) {
    	_maxIntegerDigitsAttr = maxIntegerDigits;
    }
    
    public void setMinIntegerDigits(ProcessorProperty minIntegerDigits) {
    	_minIntegerDigitsAttr = minIntegerDigits;
    }
    
    public void setMaxFractionDigits(ProcessorProperty maxFractionDigits) {
    	_maxFractionDigitsAttr = maxFractionDigits;
    }
    
    public void setMinFractionDigits(ProcessorProperty minFractionDigits) {
    	_minFractionDigitsAttr = minFractionDigits;
    }
    
    public void setVar(String var) {
    	_var = var;
    }

    public void setScope(String scope) {
		_scope = scope;
    }
}

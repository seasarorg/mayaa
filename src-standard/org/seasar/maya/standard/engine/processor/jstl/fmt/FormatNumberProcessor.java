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

/**
 * JSTL の fmt:formatNumber にあたるネイティブプロセッサ.
 * 
 * @author duran
 *  
 */
public class FormatNumberProcessor extends AbstractBodyProcessor {

    private static final long serialVersionUID = 2352354341113789083L;
    private static final String NUMBER 	= "number";
    private static final String CURRENCY 	= "currency";
    private static final String PERCENT 	= "percent";
    
    private ProcessorProperty _typeAttr;
    private ProcessorProperty _patternAttr;
    private ProcessorProperty _currencyCodeAttr;
    private ProcessorProperty _currencySymbolAttr;
    private ProcessorProperty	_groupingUsedAttr;
    private ProcessorProperty _maxIntegerDigitsAttr;
    private ProcessorProperty	_minIntegerDigitsAttr;
    private ProcessorProperty _maxFractionDigitsAttr;
    private ProcessorProperty _minFractionDigitsAttr;

    
    private String _type;
    private String _pattern;
    private String _currencyCode;
    private String _currencySymbol;
    private boolean	_groupingUsed = true;
    private int _maxIntegerDigits = -1;
    private int	_minIntegerDigits = -1;
    private int _maxFractionDigits = -1;
    private int _minFractionDigits = -1;

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
    
    private int parseInt(ProcessorProperty prop, ServiceCycle cycle){
        if(prop != null && cycle != null){
            Object obj = prop.getValue(cycle);
            if(obj instanceof Integer){
                return ((Integer)obj).intValue();
            }else if(obj instanceof String){
                return Integer.parseInt((String)obj);
            }
        }
        return -1;
    }

    private String parseString(ProcessorProperty prop, ServiceCycle cycle){
        if(prop != null && cycle != null){
            Object obj = prop.getValue(cycle);
            if(obj instanceof String){
                return (String)obj;
            }
        }
        return null;
    }
    
    private void initParameter(ServiceCycle cycle){
        _type = parseString(_typeAttr,cycle);
        _pattern = parseString(_patternAttr,cycle);
        _currencyCode = parseString(_currencyCodeAttr,cycle);
        _currencySymbol = parseString(_currencySymbolAttr,cycle);
        
        if(_groupingUsedAttr != null){
            Object obj = _groupingUsedAttr.getValue(cycle);
        	if(obj instanceof Boolean) {
        	    _groupingUsed = ((Boolean)obj).booleanValue();
        	} else if(obj instanceof String) {
        	    _groupingUsed = Boolean.valueOf((String)obj).booleanValue();
        	}
        } else {
            //デフォルト true
            _groupingUsed = true;
        }
        
        _maxIntegerDigits = parseInt(_maxIntegerDigitsAttr,cycle);
        _minIntegerDigits = parseInt(_minIntegerDigitsAttr,cycle);
        _maxFractionDigits = parseInt(_maxFractionDigitsAttr,cycle);
        _minFractionDigits = parseInt(_minFractionDigitsAttr,cycle);
        
    }
    
    protected ProcessStatus process(ServiceCycle cycle, Object obj) {
        String formatted = null;
        Object input = obj;
        
        initParameter(cycle);

        if (input instanceof String) {
            
            if(input.equals("")){
                return EVAL_PAGE;
            }
            
            try {
                if (((String) input).indexOf('.') != -1) {
                    input = Double.valueOf((String) input);
                } else {
                    input = Long.valueOf((String) input);
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalTagAttributeException(getTemplate(),
                		"formatNumber", "value", obj, null);
            }
        } else if(input instanceof Number){
                input = obj;
        } else {
            throw new IllegalTagAttributeException(getTemplate(),
                    "formatNumber", "value", obj, null);
        }

        Locale locale = getLocale(cycle);

        if (locale != null) {
            // Create formatter
            NumberFormat formatter = null;
            
            
            
            if ((_pattern != null) && !_pattern.equals("")) {
                // if 'pattern' is specified, 'type' is ignored
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
                formatter = new DecimalFormat(_pattern, symbols);
            } else {
                formatter = createFormatter(locale,cycle);
            }
            
            
            if (((_pattern != null) && !_pattern.equals(""))
                    || CURRENCY.equalsIgnoreCase(_type)) {
                try {
                    setCurrency(formatter);
                } catch (Exception e) {
                    //TODO
                }
            }
            configureFormatter(formatter, cycle);
            formatted = formatter.format(input);
        } else {
            // no formatting locale available, use toString()
            formatted = input.toString();
        }

        if (_var != null) {
            AttributeScope scope = cycle.getAttributeScope(_scope);
            scope.setAttribute(_var, formatted);
        } else {
            cycle.getResponse().write(formatted);
        }

        return EVAL_PAGE;
    }
 
    /**
     * NumberFormatを作成します。<br>
     * typeがセットされていなければ<br>
     * typeは "number" "currency" "persent"<br>
     * typeの指定がなければ "number"
     * @param locale
     * @param cycle
     * @return NumberFormat
     */
    private NumberFormat createFormatter(Locale locale, ServiceCycle cycle) {

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
     * @param cycle
     */
    private void configureFormatter(NumberFormat formatter, ServiceCycle cycle) {
        formatter.setGroupingUsed(_groupingUsed);
        if (_maxIntegerDigits > -1) {
            formatter.setMaximumIntegerDigits(_maxIntegerDigits);
        }
        if (_minIntegerDigits > -1) {
            formatter.setMinimumIntegerDigits(_minIntegerDigits);
                    
        }
        if (_maxFractionDigits > -1) {
            formatter.setMaximumFractionDigits(_maxFractionDigits);
        }
        if (_minFractionDigits > -1) {
            formatter.setMinimumFractionDigits(_minFractionDigits);
        }
    }

    /**
     * Locale を取得する。 ページコンテキスト内の FMT_LOCALE を探し、あればそれを返す。 無ければデフォルト Locale を返す。
     * 
     * @param cycle
     *            評価時のページコンテキスト
     * @return Locale
     */
    protected Locale getLocale(ServiceCycle cycle) {
        // TODO Localization
        Object locale = CycleUtil.findAttribute(cycle, FMT_LOCALE);

        if (locale instanceof Locale) {
            return (Locale) locale;
        } else if (locale instanceof String) {
            return parseLocale((String) locale);
        } else {
            // TODO Locale のデフォルトの決定 (null or getDefault)
            // return null;
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

        if ((index = localeString.indexOf('-')) >= 0
                || (index = localeString.indexOf('_')) >= 0) {
            language = localeString.substring(0, index);
            country = localeString.substring(index + 1);
        } else {
            language = localeString;
            country = null;
        }

        if (StringUtil.isEmpty(language)
                || (country != null && country.length() == 0)) {
            throw new IllegalArgumentException("invalid Locale: "
                    + localeString);
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
        String code = null;
        String symbol = null;

        if ((_currencyCode == null) && (_currencySymbol == null)) {
			return;
		}

		if ((_currencyCode != null) && (_currencySymbol != null)) {
			if (currencyClass != null) {
				code = _currencyCode;
			} else {
				symbol = _currencySymbol;
			}
		} else if (_currencyCode == null) {
			symbol = _currencySymbol;
		} else {
			if (currencyClass != null) {
				code = _currencyCode;
			} else {
				symbol = _currencyCode;
			}
		}

        if (code != null) {
            Object[] methodArgs = new Object[1];

            /*
			 * java.util.Currency.getInstance()
			 */
            Method m = currencyClass.getMethod("getInstance",
                    GET_INSTANCE_PARAM_TYPES);
            methodArgs[0] = code;
            Object currency = m.invoke(null, methodArgs);

            /*
             * java.text.NumberFormat.setCurrency()
             */
            Class[] paramTypes = new Class[1];
            paramTypes[0] = currencyClass;
            Class numberFormatClass = Class.forName("java.text.NumberFormat");
            m = numberFormatClass.getMethod("setCurrency", paramTypes);
            methodArgs[0] = currency;
            m.invoke(formatter, methodArgs);
        } else {
            /*
             * Let potential ClassCastException propagate up (will almost never
             * happen)
             */
            DecimalFormat df = (DecimalFormat) formatter;
            DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
            dfs.setCurrencySymbol(symbol);
            df.setDecimalFormatSymbols(dfs);
        }
    }

    public boolean isChildEvaluation(ServiceCycle context) {
        return true;
    }
    
}

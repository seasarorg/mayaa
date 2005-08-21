package org.seasar.maya.standard.engine.processor.jstl.fmt;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.seasar.maya.impl.util.StringUtil;

public class FormatUtil {
    public static NumberFormat getFormat(Locale locale,String pattern, String type) {
        if( StringUtil.isEmpty(pattern) == false )
            return new DecimalFormat(pattern);
        if("NUMBER".equals(type))
            return NumberFormat.getNumberInstance(locale);
        if("CURRENCY".equals(type))
            return NumberFormat.getCurrencyInstance(locale);
        if("PERCENT".equals(type))
            return NumberFormat.getPercentInstance(locale);        
        return NumberFormat.getInstance(locale);
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
    public static Locale parseLocale(String localeString) {
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
}

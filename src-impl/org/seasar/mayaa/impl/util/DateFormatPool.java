/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.impl.util.collection.AbstractSoftReferencePool;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class DateFormatPool {

    private static Map _formatPools =
            Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true));

    private DateFormatPool() {
        throw new UnsupportedOperationException();
    }

    public static DateFormat borrowFormat(String formatPattern) {
        return borrowFormat(formatPattern, Locale.getDefault());
    }

    private static String makeKey(String formatPattern, Locale locale) {
        return formatPattern + "\n" + locale.toString();
    }

    public static DateFormat borrowFormat(String formatPattern, Locale locale) {
        String key = makeKey(formatPattern, locale);
        Pool pool = (Pool) _formatPools.get(key);
        if (pool == null) {
            pool = new Pool(formatPattern, locale);
            _formatPools.put(key, pool);
        }
        return pool.borrowFormat();
    }

    public static void returnFormat(DateFormat object) {
        if (object instanceof SimpleLocaleDateFormat == false) {
            return;
        }
        SimpleLocaleDateFormat format = (SimpleLocaleDateFormat) object;

        String key = makeKey(format.toPattern(), format.getLocale());
        Pool pool = (Pool) _formatPools.get(key);
        if (pool != null) {
            pool.returnFormat(format);
        }
    }

    public static DateFormat borrowRFC1123Format() {
        DateFormat result = borrowFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        return result;
    }

    public static DateFormat borrowRFC2822Format() {
        DateFormat result = borrowFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        return result;
    }

    // support class

    private static class Pool extends AbstractSoftReferencePool {

        private static final long serialVersionUID = 32939508346669867L;

        private String _formatPattern;
        private Locale _locale;

        public Pool(String formatPattern, Locale locale) {
            if (formatPattern == null) {
                throw new IllegalArgumentException();
            }
            _formatPattern = formatPattern;
            _locale = locale;
        }

        protected Object createObject() {
            DateFormat result = new SimpleLocaleDateFormat(_formatPattern, _locale);
            return result;
        }

        protected boolean validateObject(Object object) {
            return object instanceof DateFormat;
        }

        public DateFormat borrowFormat() {
            return (DateFormat) borrowObject();
        }

        public void returnFormat(DateFormat format) {
            if (format != null) {
                returnObject(format);
            }
        }

    }

    private static class SimpleLocaleDateFormat extends SimpleDateFormat {
        private static final long serialVersionUID = 1L;

        private Locale _locale;

        public SimpleLocaleDateFormat(String pattern, Locale locale) {
            super(pattern, locale);
            _locale = locale;
        }

        public Locale getLocale() {
            return _locale;
        }
    }
}


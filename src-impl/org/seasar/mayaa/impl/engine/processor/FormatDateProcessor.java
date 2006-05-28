/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.collection.AbstractSoftReferencePool;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FormatDateProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -3881431416799800307L;

    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private String _pattern;
    private static Map _formatPools = new HashMap();

    public void initialize() {
        if (_pattern == null) {
            _pattern = new SimpleDateFormat().toPattern();
        }
    }

    // MLD property, expectedClass=java.lang.String
    public void setValue(ProcessorProperty value) {
        _value = value;
    }

    public void setDefault(ProcessorProperty defaultValue) {
        _default = defaultValue;
    }

    public void setPattern(String pattern) {
        _pattern = pattern;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_value != null) {
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().write(format(_value));
        }
        return ProcessStatus.SKIP_BODY;
    }

    private String format(ProcessorProperty property) {
        Object result = property.getValue().execute(null);
        if (result != null) {
            if (result instanceof Date) {
                DateFormatPool pool = getFormatPool();

                DateFormat formatter = pool.borrowFormat();
                String formattedValue = formatter.format(result);

                pool.returnFormat(formatter);
                return formattedValue;
            }

            throw new IllegalArgumentException(
                    "argument type mismatch: " + result.getClass().getName());
        } else if (_default != null) {
            return (String) _default.getValue().execute(null);
        }
        return "";
    }

    protected DateFormatPool getFormatPool() {
        synchronized (_formatPools) {
            DateFormatPool pool = (DateFormatPool) _formatPools.get(_pattern);
            if (pool == null) {
                pool = new DateFormatPool(_pattern);
                _formatPools.put(_pattern, pool);
            }
            return pool;
        }
    }

    protected class DateFormatPool extends AbstractSoftReferencePool {

        private static final long serialVersionUID = 32939508346669867L;

        private String _formatPattern;

        public DateFormatPool(String formatPattern) {
            if (formatPattern == null) {
                throw new IllegalArgumentException();
            }
            _formatPattern = formatPattern;
        }

        protected Object createObject() {
            return new SimpleDateFormat(_formatPattern);
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

}

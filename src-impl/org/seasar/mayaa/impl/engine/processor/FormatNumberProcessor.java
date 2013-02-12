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
package org.seasar.mayaa.impl.engine.processor;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceMap;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.collection.AbstractSoftReferencePool;

/**
 * 数値を指定フォーマットで文字列に変換して出力するプロセッサ。
 * 内部的には{@link DecimalFormat}。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FormatNumberProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = 5513075315121041838L;

    private static Map _formatPools =
            Collections.synchronizedMap(new ReferenceMap(AbstractReferenceMap.SOFT, AbstractReferenceMap.SOFT, true));

    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private String _pattern;

    public void initialize() {
        if (_pattern == null) {
            _pattern = new DecimalFormat().toPattern();
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
            if (result instanceof String && ((String) result).length() > 0) {
                result = ObjectUtil.numberValue(result, null);
            }
            if (result instanceof Number) {
                NumberFormatPool pool = getFormatPool();

                NumberFormat formatter = pool.borrowFormat();
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

    protected NumberFormatPool getFormatPool() {
        synchronized (_formatPools) {
            NumberFormatPool pool = (NumberFormatPool) _formatPools.get(_pattern);
            if (pool == null) {
                pool = new NumberFormatPool(_pattern);
                _formatPools.put(_pattern, pool);
            }
            return pool;
        }
    }

    // support class

    protected static class NumberFormatPool extends AbstractSoftReferencePool {

        private static final long serialVersionUID = -4295432835558317767L;

        private String _formatPattern;

        public NumberFormatPool(String formatPattern) {
            if (formatPattern == null) {
                throw new IllegalArgumentException();
            }
            _formatPattern = formatPattern;
        }

        protected Object createObject() {
            return new DecimalFormat(_formatPattern);
        }

        protected boolean validateObject(Object object) {
            return object instanceof NumberFormat;
        }

        public NumberFormat borrowFormat() {
            return (NumberFormat) borrowObject();
        }

        public void returnFormat(NumberFormat format) {
            if (format != null) {
                returnObject(format);
            }
        }

    }

}

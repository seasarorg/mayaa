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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.util.DateFormatPool;

/**
 * {@link java.util.Date}を指定フォーマットで文字列に変換して出力するプロセッサ。
 * 内部的には{@link SimpleDateFormat}。
 *
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FormatDateProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -2331626109260967664L;

    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private String _pattern;

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
                DateFormat formatter = DateFormatPool.borrowFormat(_pattern);
                String formattedValue = formatter.format(result);
                DateFormatPool.returnFormat(formatter);
                return formattedValue;
            }

            throw new IllegalArgumentException(
                    "argument type mismatch: " + result.getClass().getName());
        } else if (_default != null) {
            return (String) _default.getValue().execute(null);
        }
        return "";
    }

}

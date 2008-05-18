/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.engine.processor.EchoProcessor;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * test属性がtrueの場合のみechoと同じ動作をするプロセッサ。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class EchoIfProcessor extends EchoProcessor {

    private static final long serialVersionUID = -2524202747941196473L;

    private static final String TEST_KEY = EchoProcessor.class.getName() + "#test";
    static {
        CycleUtil.registVariableFactory(TEST_KEY,
                new DefaultCycleLocalInstantiator() {
            public Object create(Object owner, Object[] params) {
                return Boolean.FALSE;
            }
        });
    }

    private ProcessorProperty _test;

    // MLD property, expectedClass=boolean
    public void setTest(ProcessorProperty test) {
        if (test == null) {
            throw new IllegalArgumentException();
        }
        _test = test;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_test == null) {
            throw new IllegalStateException();
        }
        boolean test = ObjectUtil.booleanValue(_test.getValue().execute(null), false);
        CycleUtil.setLocalVariable(TEST_KEY, this, Boolean.valueOf(test));
        return test ? super.doStartProcess(topLevelPage) : ProcessStatus.SKIP_BODY;
    }

    public ProcessStatus doEndProcess() {
        boolean test = ((Boolean)CycleUtil.getLocalVariable(
                TEST_KEY, this, null)).booleanValue();
        return test ? super.doEndProcess() : ProcessStatus.EVAL_PAGE;
    }

}

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

import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * ビルド時に確定したディレクティブを実行時に反映する。
 */
public class DirectiveProcessor extends TemplateProcessorSupport {

    private static final long serialVersionUID = -5887789605135227245L;

    private static final String AUTO_ESCAPE_DIRECTIVE_NAME = "autoEscape";

    private String _directiveName;
    private String _directiveValue;

    public void setDirectiveName(String directiveName) {
        _directiveName = directiveName;
    }

    public String getDirectiveName() {
        return _directiveName;
    }

    public void setDirectiveValue(String directiveValue) {
        _directiveValue = directiveValue;
    }

    public String getDirectiveValue() {
        return _directiveValue;
    }

    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (AUTO_ESCAPE_DIRECTIVE_NAME.equalsIgnoreCase(_directiveName)
                && StringUtil.hasValue(_directiveValue)) {
            AutoEscapeContext.setPageAutoEscapeEnabled(
                    Boolean.valueOf(_directiveValue.toLowerCase()));
        }
        return ProcessStatus.SKIP_BODY;
    }
}

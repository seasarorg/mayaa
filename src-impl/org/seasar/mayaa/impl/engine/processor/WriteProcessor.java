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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.mayaa.cycle.CycleWriter;
import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WriteProcessor extends AbstractAttributableProcessor {

    private static final long serialVersionUID = 9014904982423846249L;
    private static final String BODY_VARIABLE_NAME = "bodyText";
    private boolean _forHTML;
    private boolean _forHyperText;
    private ProcessorProperty _value;
    private ProcessorProperty _default;
    private ProcessorProperty _escapeXml;
    private ProcessorProperty _escapeWhitespace;
    private ProcessorProperty _escapeEol;

    public void initialize() {
        QName originalQName = getOriginalNode().getQName();
        _forHTML = isHTML(originalQName);
        _forHyperText = _forHTML || isXHTML(originalQName);
        boolean needBodyText;
        if (_value == null) {
            needBodyText = (_injectedNode.getChildNodeSize() > 0);
        } else {
            if (_value.getValue().isLiteral()) {
                needBodyText = false;
            } else {
                // bodyTextを含むスクリプトか？
                needBodyText = isExistsBodyTextInScript(_value.getValue().getScriptText());
            }
        }
        setChildEvaluation(needBodyText);
    }

    protected boolean isExistsBodyTextInScript(String scriptText) {
        String pattern = ".*("
            + "\\$\\{"+BODY_VARIABLE_NAME+"\\}|"
            + "\\$\\{"+BODY_VARIABLE_NAME+"[^a-zA-Z_$].*\\}|"
            + "\\$\\{.*[^a-zA-Z_$]"+BODY_VARIABLE_NAME+"\\}|"
            + "\\$\\{.*[^a-zA-Z_$]"+BODY_VARIABLE_NAME+"[^a-zA-Z_$].*\\}"
            + ")+.*";
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.MULTILINE);
        Matcher m = p.matcher(scriptText);
        return m.matches();
    }

    // MLD property, expectedClass=java.lang.String
    public void setValue(ProcessorProperty value) {
        _value = value;
    }

    public void setDefault(ProcessorProperty defaultValue) {
        _default = defaultValue;
    }

    public void setEscapeXml(ProcessorProperty escapeXml) {
        ProcessorUtil.checkBoolableProperty(escapeXml);
        _escapeXml = escapeXml;
    }

    public void setEscapeWhitespace(ProcessorProperty escapeWhitespace) {
        ProcessorUtil.checkBoolableProperty(escapeWhitespace);
        _escapeWhitespace = escapeWhitespace;
    }

    public void setEscapeEol(ProcessorProperty escapeEol) {
        ProcessorUtil.checkBoolableProperty(escapeEol);
        _escapeEol = escapeEol;
    }

    private void writeValue(String literal) {
        Object result;
        if (literal != null) {
            result = literal;
        } else {
            if (_value == null) {
                result = "";
            } else {
                result = _value.getValue().execute(null);
            }
        }
        String ret = null;
        boolean empty = StringUtil.isEmpty(result);
        if (empty && _default != null) {
            result = _default.getValue().execute(null);
            if (result != null) {
                ret = result.toString();
            }
        } else if (empty == false) {
            ret = String.valueOf(result);
            if (ProcessorUtil.toBoolean(_escapeXml)) {
                ret = StringUtil.escapeXml(ret);
            }
            if (_forHyperText && ProcessorUtil.toBoolean(_escapeEol)) {
                ret = StringUtil.escapeEol(ret, _forHTML);
            }
            if (ProcessorUtil.toBoolean(_escapeWhitespace)) {
                ret = StringUtil.escapeWhitespace(ret);
            }
        }
        if (ret != null) {
            write(ret);
        }
    }

    protected ProcessStatus writeStartElement() {
        if (isChildEvaluation()) {
            ProcesstimeInfo info = peekProcesstimeInfo();
            CycleWriter body = info.getBody();
            String bodyText = body.getString();
            body.clearBuffer();
            if (_value != null) {
                Map variables = new HashMap();
                variables.put(BODY_VARIABLE_NAME, bodyText);
                SpecificationUtil.startScope(variables);
                try {
                    writeValue(null);
                } finally {
                    SpecificationUtil.endScope();
                }
            } else {
                if (StringUtil.hasValue(bodyText)) {
                    writeValue(bodyText);
                } else {
                    writeValue("");
                }
            }
            return null;
        }

        writeValue(null);
        return ProcessStatus.SKIP_BODY;
    }

    protected void writeEndElement() {
        // no-op
    }

    protected void writeBody(String body) {
        // no-op
    }

    /**
     * サイクルに出力する
     * @param value
     */
    protected void write(String value) {
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        cycle.getResponse().write(value);
    }

}

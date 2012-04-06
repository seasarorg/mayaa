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
package org.seasar.mayaa.impl.cycle.script.rhino;

import org.mozilla.javascript.EvaluatorException;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public class OffsetLineRhinoException extends EvaluatorException {

    private static final long serialVersionUID = 2330731436282320920L;

    int _offsetLine;

    public OffsetLineRhinoException(String detail, String sourceName,
            int lineNumber, String lineSource,
            int columnNumber, int offsetLine, Throwable cause) {
        super(detail, sourceName, lineNumber, lineSource, columnNumber);
        if (cause != null) {
            initCause(cause);
        }
        _offsetLine = offsetLine;
    }

    public int getOffsetLine() {
        return _offsetLine;
    }

    public String details() {
        return super.details();
    }

    public String emphasizeDetails() {
        String message = super.details();
        String[] lines = message.split("\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            if (i == _offsetLine) {
                lines[i] = decorate(lines[i]);
            }
            sb.append(lines[i]);
            sb.append("\n");
        }
        return sb.toString();
    }

    private String decorate(String line) {
        for (int j = 0; j < line.length(); j++) {
            if (line.charAt(j) != '\t' && line.charAt(j) != ' ') {
                return line.substring(0, j) +
                    "<span style=\"color: red; text-decoration: underline\">" +
                    line.substring(j) +
                    "</span>";
            }
        }
        return line;
    }

}
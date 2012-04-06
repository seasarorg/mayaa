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
package org.seasar.mayaa.engine.processor;

import java.io.Serializable;

/**
 * プロセッサ動作にて状態遷移を示すステータス。
 *
 * @author Koji Suga (Gluegent Inc.)
 */
public class ProcessStatus implements Serializable {

    private static final long serialVersionUID = 473586899180314059L;

    private String _status;

    /**
     * @param status ステータス文字列。
     */
    public ProcessStatus(String status) {
        if (status == null) {
            throw new IllegalArgumentException();
        }
        _status = status;
    }

    /**
     * ステータス文字列を取得する。
     * @return ステータス文字列。
     */
    public String getStatus() {
        return _status;
    }

    public boolean equals(Object test) {
        if (test instanceof ProcessStatus) {
            ProcessStatus testStatus = (ProcessStatus) test;
            return testStatus.getStatus().equals(getStatus());
        }
        return false;
    }

    public int hashCode() {
        return ("org.seasar.mayaa.engine.processor.ProcessStatus:"
                + _status).hashCode();
    }

    // values ------------------------------------------------------

    /**
     * リターンフラグ。TemplateProcessor#doStartProcess()がこの値を返すと、
     * プロセッサボディを出力しない。
     */
    public static final ProcessStatus SKIP_BODY =
        new ProcessStatus("SKIP_BODY");

    /**
     * リターンフラグ。TemplateProcessor#doStartProcess()がこの値を返すと、
     * プロセッサボディをバッファリング無しで出力する。
     */
    public static final ProcessStatus EVAL_BODY_INCLUDE  =
        new ProcessStatus("EVAL_BODY_INCLUDE");

    /**
     * リターンフラグ。TemplateProcessor#doEndProcess()がこの値を返すと、
     * 以降の出力をただちに中止する。
     */
    public static final ProcessStatus SKIP_PAGE =
           new ProcessStatus("SKIP_PAGE");

    /**
     * リターンフラグ。TemplateProcessor#doEndProcess()がこの値を返すと、
     * 以降のプロセッサ出力を続ける。
     */
    public static final ProcessStatus EVAL_PAGE =
        new ProcessStatus("EVAL_PAGE");

    /**
     * リターンフラグ。この値をItarateProcessor#doAfterChildProcess()
     * が返すと、再イテレートする。
     */
    public static final ProcessStatus EVAL_BODY_AGAIN =
        new ProcessStatus("EVAL_BODY_AGAIN");

    /**
     * リターンフラグ。この値をChildEvaluationProcessor#doStartProcess()
     * が返すと、プロセッサボディをバッファリングする。
     */
    public static final ProcessStatus EVAL_BODY_BUFFERED =
        new ProcessStatus("EVAL_BODY_BUFFERED");

}

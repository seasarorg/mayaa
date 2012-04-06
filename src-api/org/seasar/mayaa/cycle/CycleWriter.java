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
package org.seasar.mayaa.cycle;

import java.io.IOException;
import java.io.Writer;

/**
 * ネストした出力構造をもつWriterオブジェクト。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class CycleWriter extends Writer {

    /**
     * コンストラクタで渡された、上位のWriterを返す。nullの場合もありえる。
     * @return 上位のWriter。
     */
    public abstract CycleWriter getEnclosingWriter();

    /**
     * バッファをクリアする。
     */
    public abstract void clearBuffer();

    /**
     * バッファの内容をStringで取得する。
     * @return バッファ内容。
     */
    public abstract String getString();

    /**
     * バッファに書き込みがされたかどうかを返す。
     * @return バッファに書き込みがされていればtrueを返す。
     */
    public abstract boolean isDirty();

    /**
     * 指定Writerにバッファ内容を書き出す。
     * @param writer 書き出し先のWriter。
     * @throws IOException
     */
    public abstract void writeOut(Writer writer) throws IOException;

}

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
package org.seasar.mayaa.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class IOUtil {

    private IOUtil() {
        // no instantiation.
    }

    /**
     * InputStreamをcloseする。
     * 例外が発生した場合はログにFATALレベルで出力する。
     *
     * @param stream closeするInputStream
     */
    public static void close(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                Log log = LogFactory.getLog(IOUtil.class);
                log.fatal(ignore.getMessage(), ignore);
            }
        }
    }

    /**
     * OutputStreamをcloseする。
     * 例外が発生した場合はログにFATALレベルで出力する。
     *
     * @param stream closeするOutputStream
     */
    public static void close(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignore) {
                Log log = LogFactory.getLog(IOUtil.class);
                log.fatal(ignore.getMessage(), ignore);
            }
        }
    }

    /**
     * InputStreamから指定文字エンコーディングでStringを読み出して返す。
     * 読み出したあとはInputStreamをcloseする。
     *
     * @param is 読み出すInputStream
     * @param encoding 文字エンコーディング
     * @return InputStreamの内容
     * @throws RuntimeException 内部で発生した例外をラップしたもの
     */
    public static String readStream(InputStream is, String encoding) {
        try {
            Reader reader = new InputStreamReader(is, encoding);
            try {
                StringBuffer sb = new StringBuffer();
                char[] buffer = new char[1024];
                int readSize = -1;
                while ((readSize = reader.read(buffer)) > 0) {
                    sb.append(buffer, 0, readSize);
                }
                return sb.toString();
            } finally {
                is.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    /**
     * OutputStreamへ指定文字エンコーディングでStringを書き出す。
     * 書き出したあとはOutputStreamをcloseする。
     *
     * @param os 書き出すInputStream
     * @param value 出力する内容
     * @param encoding 文字エンコーディング
     * @throws RuntimeException 内部で発生した例外をラップしたもの
     */
    public static void writeStream(OutputStream os, String value, String encoding) {
        try {
            Writer writer = new OutputStreamWriter(os, encoding);
            try {
                writer.write(value);
                writer.flush();
            } finally {
                os.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}

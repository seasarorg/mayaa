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
package org.seasar.mayaa.source;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.seasar.mayaa.ParameterAware;

/**
 * テンプレートファイルや設定XMLファイルのディスクリプタ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface SourceDescriptor extends Serializable, ParameterAware {

    /**
     * ソースSystemIDを設定する。
     * @param systemID
     */
    void setSystemID(String systemID);

    /**
     * ソースSystemIDを取得する。
     * @return SystemID。
     */
    String getSystemID();

    /**
     * ソースが存在するかどうかを取得する。
     * @return ファイルが存在すればtrue。無ければfalse。
     */
    boolean exists();

    /**
     * ファイルのインプットストリームを取得する。
     * @return ストリーム。もしファイルが無い場合は、null。
     */
    InputStream getInputStream();

    /**
     * ファイルの日付を取得する。
     * @return ファイルの最終更新日付。ファイルが無い場合は「new Date(0)」を返す。
     */
    Date getTimestamp();

}

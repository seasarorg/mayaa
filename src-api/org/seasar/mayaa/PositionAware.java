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
package org.seasar.mayaa;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface PositionAware {

    /**
     * ソースのSystemIDを設定する。
     * @param systemID ソースSystemID。
     */
    void setSystemID(String systemID);

    /**
     * ソースのSystemIDを取得する。
     * @return ソースSystemID。
     */
    String getSystemID();

    /**
     * ソース上の行位置を設定する。
     * @param lineNumber 位置情報。
     */
    void setLineNumber(int lineNumber);

    /**
     * ソース上の行位置を取得する。
     * @return 位置情報。
     */
    int getLineNumber();

    /**
     * ソースがテンプレートかを設定する。
     * @param onTemplate ソースがテンプレートか。
     */
    void setOnTemplate(boolean onTemplate);

    /**
     * ソースがテンプレートかを取得する。
     * @return ソースがテンプレートか。
     */
    boolean isOnTemplate();

}

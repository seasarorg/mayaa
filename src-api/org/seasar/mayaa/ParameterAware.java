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

import java.util.Iterator;

/**
 * オブジェクトのチューニング設定。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface ParameterAware extends PositionAware {

    /**
     * IS_SECURE_WEBのキー。
     */
    String SECURE_WEB_KEY = "org.seasar.mayaa.secure.web";

    /**
     * Google App Engineのような、セキュアなWeb環境設定か否か
     */
    boolean IS_SECURE_WEB = Boolean.getBoolean(SECURE_WEB_KEY);

    /**
     * ユーザー設定の受け入れメソッド。
     * @param name 設定名。
     * @param value 設定値。
     */
    void setParameter(String name, String value);

    /**
     * 設定パラメータの取得。
     * @param name 設定名。
     * @return 設定値。
     */
    String getParameter(String name);

    /**
     * 設定パラメータ名をイテレートする。
     * @return 設定パラメータ名イテレータ。
     */
    Iterator iterateParameterNames();

}

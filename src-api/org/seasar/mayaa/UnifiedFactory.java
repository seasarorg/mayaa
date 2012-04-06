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

import java.io.Serializable;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface UnifiedFactory
    extends ContextAware, ParameterAware, Serializable {

    /**
     * ファクトリの初期化。作成するサービス対象の実装クラスの設定。
     * @param serviceClass サービス対象実装クラス型。
     */
    void setServiceClass(Class serviceClass);

    /**
     * 作成するサービス対象実装クラスの取得。
     * @return サービス対象実装クラス型。
     */
    Class getServiceClass();

}

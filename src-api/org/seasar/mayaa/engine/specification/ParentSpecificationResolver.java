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
package org.seasar.mayaa.engine.specification;

import org.seasar.mayaa.ParameterAware;

/**
 * Specificationの親を取得する。
 * @author Koji Suga (Gluegent, Inc.)
 */
public interface ParentSpecificationResolver extends ParameterAware {

    /**
     * 指定した{@link Specification}の親を取得する。
     * <p>
     * 標準の実装では、テンプレートファイルの場合は対応するMayaaファイル、
     * Mayaaファイルの場合はdefault.mayaaファイルの{@link Specification}を返す。
     * default.mayaaの親はないので{@code null}を返す。
     * </p>
     * @param spec 親を探す起点となる{@link Specification}。見つからない場合は{@code null}。
     */
    Specification getParentSpecification(Specification spec);

}

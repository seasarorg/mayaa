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
package org.seasar.mayaa.builder.library;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * ライブラリ定義ソースビルダ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface DefinitionBuilder extends ParameterAware {

    /**
     * ライブラリ定義ソースからライブラリをビルドする。
     * @param source ライブラリ定義ソース。
     * @return ビルド結果。処理できなかった場合にはnullを返す。
     */
    LibraryDefinition build(SourceDescriptor source);

}

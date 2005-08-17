/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.builder.library;

import org.seasar.maya.provider.Parameterizable;
import org.seasar.maya.source.SourceDescriptor;

/**
 * ライブラリ定義ソースビルダ。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface DefinitionBuilder extends Parameterizable {

    /**
     * ライブラリ定義ソースからライブラリをビルドする。
     * @param source ライブラリ定義ソース。
     * @return ビルド結果。処理できなかった場合にはnullを返す。
     */
    LibraryDefinition build(SourceDescriptor source);
    
}

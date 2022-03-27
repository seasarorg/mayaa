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
package org.seasar.mayaa.impl.builder;

import org.seasar.mayaa.engine.specification.Namespace;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.engine.specification.NamespaceImpl;

/**
 * @author Mitsutaka WATANABE
 */
public class PageNodeHandler extends SpecificationNodeHandler {

    public PageNodeHandler(Specification specification) {
        super(specification);
    }

    @Override
    Namespace getTopLevelNamespace() {
        // Mayaaファイルを読み込むとき、明示的なPrefixMapping定義がないときは HTML4 の名前空間を使用する（現行仕様）
        URI defaultURI = URI_HTML;

        Namespace _topLevelNamespace = new NamespaceImpl();
        _topLevelNamespace.setDefaultNamespaceURI(defaultURI);
        _topLevelNamespace.addPrefixMapping("", defaultURI);
        _topLevelNamespace.addPrefixMapping("xml", URI_XML);
        return _topLevelNamespace;
    }

}

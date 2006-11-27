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
package org.seasar.mayaa.impl.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.source.SourceHolder;

/**
 * ソースディスクリプタを保持するオブジェクトを管理する
 * @author Taro Kato (Gluegent, Inc.)
 */
public class SourceHolderFactory {

    private static List _sourceHolders = new ArrayList();

    private SourceHolderFactory() {
        // no instantiate
    }

    static {
        // for context root
        SourceHolder contentRoot = new WebContextFolderSourceHolder();
        contentRoot.setRoot("/");
        appendSourceHolder(contentRoot);
    }

    public static void appendSourceHolder(SourceHolder sourceHolder) {
        synchronized(_sourceHolders) {
            if (sourceHolder == null) {
                throw new IllegalArgumentException();
            }
            _sourceHolders.add(sourceHolder);
        }
    }

    public static Iterator iterator() {
        synchronized(_sourceHolders) {
            return new ArrayList(_sourceHolders).iterator();
        }
    }

}

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
package org.seasar.mayaa.impl.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.seasar.mayaa.source.SourceHolder;

/**
 * ソースディスクリプタを保持するオブジェクトを管理する
 *
 * @author Taro Kato (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class SourceHolderFactory {

    private static List _sourceHolders;
    private static volatile boolean _immutable = false;

    private SourceHolderFactory() {
        // no instantiate
    }

    static {
        // for context root
        _sourceHolders = new ArrayList();
        SourceHolder contentRoot = new WebContextFolderSourceHolder();
        contentRoot.setRoot("/");
        appendSourceHolder(contentRoot);
    }

    /**
     * SourceHolderを追加する。
     * 一度でもiterator()が呼ばれた後は、追加しようとするとIllegalStateExceptionを
     * 投げる。
     *
     * @param sourceHolder 追加するSourceHolder
     * @throws IllegalStateException 追加不能な状態
     */
    public static void appendSourceHolder(SourceHolder sourceHolder) {
        if (_immutable) {
            throw new IllegalStateException("SourceHolderFactory is already immutable.");
        }
        synchronized(_sourceHolders) {
            if (sourceHolder == null) {
                throw new IllegalArgumentException();
            }
            _sourceHolders.add(sourceHolder);
        }
    }

    /**
     * SourceHolderのIteratorを返す。
     * 最初に呼ばれた時点でビルド済みと見なし、以降変更不可とする。
     *
     * @return SourceHolderのIterator
     */
    public static Iterator iterator() {
        if (_immutable == false) {
            // 2回目以降のsynchronizedを避けるために変更不可とする
            synchronized(_sourceHolders) {
                if (_immutable == false) {
                    _sourceHolders = Collections.unmodifiableList(_sourceHolders);
                    _immutable = true;
                }
            }
        }
        return _sourceHolders.iterator();
    }

}

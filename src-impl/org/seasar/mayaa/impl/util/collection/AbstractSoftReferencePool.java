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
package org.seasar.mayaa.impl.util.collection;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractSoftReferencePool implements Serializable {

    private static final long serialVersionUID = -4959957173402498504L;

    private List _pool;

    /**
     * 新たなインスタンスを生成して返します。
     * Poolにインスタンスが無い場合に使用されます。
     *
     * @return 新たなインスタンス
     */
    protected abstract Object createObject();

    /**
     * インスタンスがこのPoolに戻せるものか判定します。
     *
     * @param obj 判定するオブジェクト
     * @return このPoolに戻せるものならtrue
     */
    protected abstract boolean validateObject(Object obj);

    /**
     * 初期容量32のコンストラクタ。
     * 容量はインスタンス数。
     */
    public AbstractSoftReferencePool() {
        this(32);
    }

    /**
     * 初期容量を指定するコンストラクタ。
     * 容量はインスタンス数。
     *
     * @param initialCapacity 初期容量
     */
    public AbstractSoftReferencePool(int initialCapacity) {
        _pool = new ArrayList(initialCapacity);
    }

    protected Object borrowObject() {
        Object obj = null;
        synchronized(_pool) {
            while (obj == null) {
                if (_pool.isEmpty()) {
                    obj = createObject();
                } else {
                    SoftReference ref = (SoftReference) _pool.remove(_pool.size() - 1);
                    obj = ref.get();
                }
            }
        }
        return obj;
    }

    protected void returnObject(Object obj) {
        if (validateObject(obj)) {
            synchronized (_pool) {
                _pool.add(new SoftReference(obj));
            }
        }
    }

}

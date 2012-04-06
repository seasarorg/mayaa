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
package org.seasar.mayaa.engine.specification.serialize;

import org.seasar.mayaa.engine.processor.ProcessorTreeWalker;

/**
 * プロセッサの復元を通知する。
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface ProcessorResolveListener {

    /**
     * デシリアライズに復元しようとしているプロセッサ参照を解決する
     * オリジナルプロセッサがロードされた際に非同期に通知する。
     * @param uniqueID 要求していたプロセッサID
     * @param loadedInstance 復元された対象プロセッサ。ヌルの場合は見つからなかったことを示す。
     */
    void notify(String uniqueID, ProcessorTreeWalker loadedInstance);

    /**
     * 復元処理が終わった際に呼び出される。
     */
    void release();

}


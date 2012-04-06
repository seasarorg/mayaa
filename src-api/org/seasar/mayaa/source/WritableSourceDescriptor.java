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
package org.seasar.mayaa.source;

import java.io.OutputStream;

/**
 * 書き込み可能なテンプレートファイルや設定XMLファイルのディスクリプタ。
 * 実リソースに書き込み可能かどうかはcanWriteメソッドで判定する。
 *
 * @author Koji Suga (Gluegent, Inc.)
 * @since 1.1.4
 */
public interface WritableSourceDescriptor extends SourceDescriptor {

    /**
     * 書き込み可能かどうかを取得する。
     * @return 書き込み可能ならtrue。可能でなければfalse。
     */
    boolean canWrite();

    /**
     * ファイルのアウトプットストリームを取得する。
     * @return ストリーム。もし書き込めない場合は、null。
     */
    OutputStream getOutputStream();

}

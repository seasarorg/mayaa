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

import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Taro Kato (Gluegent, Inc.)
 */
public interface SourceDescriptorObserver {

    /**
     * 登録されているSourceDescriptorを通知する。
     *
     * @param sourceDescriptor
     *            登録されているSourceDescriptor
     * @return 次の登録SourceDescriptorの通知を受けたい場合は trueを返すこと。 検索を終了したい場合は false を返すこと。
     */
    boolean nextSourceDescriptor(SourceDescriptor sourceDescriptor);

}

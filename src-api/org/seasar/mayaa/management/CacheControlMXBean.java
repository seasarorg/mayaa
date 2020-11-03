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
package org.seasar.mayaa.management;

import javax.management.MXBean;

/**
 * Mayaa内で使用されている各種キャッシュの状態の取得や操作を行うためのMBeanインタフェース．
 * ObjectNameは "org.seasar.mayaa:type=CacheControl,name=[キャッシュ名称]" である．
 * 
 * @since 1.2
 * @author Watanabe, Mitsutaka
 */
@MXBean
public interface CacheControlMXBean {
    /** DOMAIN:type=TYPE,name=NAME */
    static final String JMX_OBJECT_NAME_FORMAT = "org.seasar.mayaa:type=CacheControl,name=%s";

    int getRetainSize();
    void setRetainSize(int retainSize);
    long getCurrentSize();
    long getHitCount();
    long getMissCount();
    String getClassName();
}
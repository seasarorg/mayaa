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
package org.seasar.mayaa.builder;

import org.seasar.mayaa.ParameterAware;
import org.seasar.mayaa.engine.specification.QName;

/**
 * HTMLタグ属性の相対パスを絶対パスに置換する。
 * @author Koji Suga (Gluegent, Inc.)
 */
public interface PathAdjuster extends ParameterAware {

    /**
     * 置換対象の属性を持つタグか判定する。
     * @param nodeName 対象タグのQName
     * @return 置換対象の属性を持つタグならtrue
     */
    boolean isTargetNode(QName nodeName);

    /**
     * 置換対象の属性か判定する。
     * @param nodeName 対象タグのQName
     * @param attributeName 対象属性のQName
     * @return 置換対象の属性ならtrue
     */
    boolean isTargetAttribute(QName nodeName, QName attributeName);

    /**
     * 相対パスを絶対パスに置換する。
     * @param base 相対パスの起点(リソース名まで含む)
     * @param path 置換対象の相対パス
     * @return 相対パスなら絶対パスに置換する
     */
    String adjustRelativePath(String base, String path);

}

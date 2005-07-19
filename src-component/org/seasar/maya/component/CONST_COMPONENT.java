/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.component;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.CONST_IMPL;

/**
 * 実装で必要な定数の定義。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public interface CONST_COMPONENT {

    String URI_COMPONENT = "http://maya.seasar.org/component";
    String PREFIX_PAGE = CONST_IMPL.PREFIX_PAGE;
    
    /*
     * Known QName
     */
    QName QP_COMPONENT_PAGE = new QName(URI_COMPONENT, "componentPage");
    QName QP_PATH = new QName(URI_COMPONENT, "path");
    QName QP_NAMESPACE_URI = new QName(URI_COMPONENT, "namespaceURI");

}

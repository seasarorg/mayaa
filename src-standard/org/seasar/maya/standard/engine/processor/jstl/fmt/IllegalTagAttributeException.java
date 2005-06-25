/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 *
 * Created on 2005/03/26
 */
package org.seasar.maya.standard.engine.processor.jstl.fmt;

import org.seasar.maya.engine.Template;
import org.seasar.maya.impl.MayaException;

/**
 * ネイティブ JSTL タグにセットした属性が不正な例外。
 * @author suga
 */
public class IllegalTagAttributeException extends MayaException {
    /** 属性が見つからなかったテンプレート */
    private Template _template;
    /** 対象のタグの名前 */
    private String _tagName;
    /** 不正な値が渡された属性の名前 */
    private String _attributeName;
    /** 不正な値 */
    private Object _attributeValue;
    /** 正しい入力例などのメッセージ */
    private String _message;

    public IllegalTagAttributeException(
            Template template, String tagName,
            String attributeName, Object attributeValue, String message) {
        _template = template;
        _tagName = tagName;
        _attributeName = attributeName;
        _attributeValue = attributeValue;
        _message = message;
    }

    public String getAttributeName() {
        return _attributeName;
    }

    public Object getAttributeValue() {
        return _attributeValue;
    }

    public String getMessage() {
        return _message;
    }

    public String getTagName() {
        return _tagName;
    }

    public Template getTemplate() {
        return _template;
    }

    public void setTemplate(Template template) {
        _template = template;
    }
}

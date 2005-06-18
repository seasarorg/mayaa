/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.standard.engine.processor.jsp;

import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.collection.AbstractSoftReferencePool;

/**
 * 特定カスタムタグのインスタンスをプールする.
 * @author suga
 * @author higa (original)
 */
public class TagPoolEntry {

    /** カスタムタグのクラス */
    private Class _tagClass;

    /** カスタムタグのプール */
    private JspCustomTagPool _jspCustomTagPool = new JspCustomTagPool();

    /**
     * コンストラクタ.
     * @param tagClass カスタムタグのクラス。
     */
    public TagPoolEntry(Class tagClass) {
        if(tagClass == null) {
            throw new IllegalArgumentException();
        }
        _tagClass = tagClass;
    }

    /**
     * カスタムタグのインスタンスを取得する.
     * プールにあるならそれを、無ければ新しいインスタンスを作成して返す.
     * @return タグのインスタンス
     */
    public Tag request() {
        return _jspCustomTagPool.borrowTag();
    }

    /**
     * カスタムタグのインスタンスをプールに戻す.
     * @param tag プールに戻すカスタムタグのインスタンス
     */
    public void release(Tag tag) {
        _jspCustomTagPool.returnTag(tag);
    }

    /** カスタムタグのプール */
    private class JspCustomTagPool extends AbstractSoftReferencePool {
        
        protected Object createObject() {
            return ObjectUtil.newInstance(_tagClass);
        }
        
        protected boolean validateObject(Object object) {
            return object instanceof Tag;
        }
        
        private Tag borrowTag() {
            return (Tag)borrowObject();
        }
        
        private void returnTag(Tag tag) {
            returnObject(tag);
        }
        
    }
    
}

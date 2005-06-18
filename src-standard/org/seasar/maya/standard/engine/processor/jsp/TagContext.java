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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessor;

/**
 * @author suga
 */
public class TagContext {

    /**
     * ステートフル情報を保持するコンテキストオブジェクトの取得を行う。
     * @param context カレントのテンプレートコンテキスト。
     * @return コンテキストオブジェクト。
     *          テンプレートコンテキスト中に保持していないときには生成する。
     */
    public static TagContext getTagContext(PageContext context) {
        final String KEY_TAG_CONTEXT = TagContext.class.getName();
        TagContext tagContext = (TagContext)context.getAttribute(KEY_TAG_CONTEXT);
        if(tagContext == null) {
            tagContext = new TagContext();
            context.setAttribute(KEY_TAG_CONTEXT, tagContext);
        }
        return tagContext;
    }

    private Map _tagPoolEntries = new HashMap();
    
    private Map _loadedTagMap = new HashMap();
    private Map _nestedVariableMap = new HashMap();
    private Map _nestedVariableNameMap = new HashMap();

    private TagPoolEntry getEntry(Class tagClass, String attributesKey) {
        synchronized (_tagPoolEntries) {
	        String key = tagClass.getName() + attributesKey;
	        TagPoolEntry entry = (TagPoolEntry)_tagPoolEntries.get(key);
	        if(entry == null) {
                entry = new TagPoolEntry(tagClass);
                _tagPoolEntries.put(key, entry);
            }
	        return entry;
        }
    }
    
    /**
     * クラス名を指定してカスタムタグを取得する。
     * @param tagClassName カスタムタグのクラス名。
     * @param attributesKey 属性の組み合わせ識別キー
     * @return カスタムタグのインスタンス。
     */
    public Tag loadTag(Class tagClass, String attributesKey) {
        TagPoolEntry entry = getEntry(tagClass, attributesKey);
        return entry.request();
    }
    
    /**
     * ロードしたJSPカスタムタグを保存する。TemplateProcessorがリクエストに対して
     * ステートレスなつくりのために、ステートフルなJSPカスタムタグをコンテキストで
     * ハンドルする必要がある。
     * @param templateProcessor 処理を行うTemplateProcessor。
     * @param customTag	インジェクトされたJSPカスタムタグ。
     */
    public void putLoadedTag(TemplateProcessor templateProcessor, Tag customTag) {
        _loadedTagMap.put(templateProcessor, customTag);
    }
    
    /**
     * ロード済みのJSPカスタムタグを取得する。
     * @param templateProcessor 処理を行うTemplateProcessor。
     * @return インジェクトされたJSPカスタムタグ。
     */
    public Tag getLoadedTag(TemplateProcessor templateProcessor) {
        return (Tag)_loadedTagMap.get(templateProcessor);
    }

    /**
     * JSPカスタムタグネスト時に一時待避するNESTED変数Mapを保存する。
     * @param customTag インジェクトされたJSPカスタムタグ。
     * @param variables カスタムタグのNESTED変数の値。
     */
    public void putNestedVariables(Tag customTag, Map variables) {
        _nestedVariableMap.put(customTag, variables);
    }

    /**
     * 一時待避したJSPカスタムタグのNESTED変数Mapを取得する。
     * @param customTag インジェクトされたJSPカスタムタグ。
     * @return カスタムタグのNESTED変数の値。
     */
    public Map getNestedVariables(Tag customTag) {
        return (Map)_nestedVariableMap.get(customTag);
    }

    /**
     * JSPカスタムタグのNESTED変数名を保存する。
     * @param customTag インジェクトされたJSPカスタムタグ。
     * @param variableNames カスタムタグ用のNESTED変数名
     */
    public void putNestedVariableNames(Tag customTag, Collection variableNames) {
        _nestedVariableNameMap.put(customTag, variableNames);
    }

    /**
     * JSPカスタムタグのNESTED変数名を取得する。
     * @param customTag インジェクトされたJSPカスタムタグ。
     * @return JSPカスタムタグの変数名。
     */
    public Collection getNestedVariableNames(Tag customTag) {
        return (Collection)_nestedVariableNameMap.get(customTag);
    }

    /**
     * カスタムタグをプールに戻す.
     * @param tag プールに戻すカスタムタグ
     * @param attributesKey 属性の組み合わせ識別キー
     */
    public void releaseTag(Tag tag, String attributesKey) {
        TagPoolEntry entry = getEntry(tag.getClass(), attributesKey);
        entry.release(tag);
    }
    
}

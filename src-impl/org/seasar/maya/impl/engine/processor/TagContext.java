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
package org.seasar.maya.impl.engine.processor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.util.CycleUtil;

/**
 * @author suga
 */
public class TagContext {

    public static TagContext getTagContext() {
        final String KEY_TAG_CONTEXT = TagContext.class.getName();
        TagContext tagContext = 
            (TagContext)CycleUtil.getAttribute(KEY_TAG_CONTEXT);
        if(tagContext == null) {
            tagContext = new TagContext();
            CycleUtil.setAttribute(KEY_TAG_CONTEXT, tagContext);
        }
        return tagContext;
    }

    private Map _tagPoolEntries = new HashMap();
    private Map _loadedTagMap = new HashMap();

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
    
    public Tag loadTag(Class tagClass, String attributesKey) {
        TagPoolEntry entry = getEntry(tagClass, attributesKey);
        return entry.request();
    }
    
    public void putLoadedTag(TemplateProcessor templateProcessor,
            Tag customTag) {
        _loadedTagMap.put(templateProcessor, customTag);
    }
    
    public Tag getLoadedTag(TemplateProcessor templateProcessor) {
        return (Tag)_loadedTagMap.get(templateProcessor);
    }

    public void releaseTag(Tag tag, String attributesKey) {
        TagPoolEntry entry = getEntry(tag.getClass(), attributesKey);
        entry.release(tag);
    }
    
}

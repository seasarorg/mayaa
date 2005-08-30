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
package org.seasar.maya.impl.builder.library.tld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.builder.library.JspProcessorDefinition;
import org.seasar.maya.impl.builder.library.JspPropertyDefinition;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * tldのattributeタグ用ハンドラ.
 * @author suga
 */
public class AttributeTagHandler extends TagHandler {

    private static final Log LOG = LogFactory.getLog(AttributeTagHandler.class);
            
    JspPropertyDefinition _property;
    private TagTagHandler _parent;

    public AttributeTagHandler(TagTagHandler parent) {
        super("attribute");
        _parent = parent;
        putHandler(new TagHandler("name") {
            protected void end(String body) {
                _property.setName(body);
            }
        });
        putHandler(new TagHandler("required") {
            protected void end(String body) {
                try {
                    _property.setRequired(ObjectUtil.booleanValue(body, false));
                } catch (RuntimeException e) {
                    if(LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    _parent.invalidate();
                }
            }
        });
        putHandler(new TagHandler("type") {
            protected void end(String body) {
                try {
                    Class expetedType = ObjectUtil.loadClass(body);
                    _property.setExpectedType(expetedType);
                } catch (RuntimeException e) {
                    if(LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    _parent.invalidate();
                } 
            }
        });
    }

    protected void start(Attributes attributes) {
        _property = new JspPropertyDefinition();
    }

    protected void end(String body) {
        JspProcessorDefinition processor = _parent.getProcessorDefinition();
        _property.setProcessorDefinition(_parent.getProcessorDefinition());
        processor.addPropertyDefinitiion(_property);
    }

}

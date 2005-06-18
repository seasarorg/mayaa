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
package org.seasar.maya.standard.builder.library.scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.seasar.maya.standard.builder.library.JspProcessorDefinition;
import org.seasar.maya.standard.builder.library.JspPropertyDefinition;
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
        _parent = parent;
        putHandler("name", new TagHandler() {
            protected void end(String body) {
                _property.setName(body);
            }
        });
        putHandler("required", new RequiredSetter(this));
        putHandler("type", new TagHandler() {
            protected void end(String body) {
                _property.setExpectedType(body);
            }
        });
    }

    protected void start(Attributes attributes) {
        _property = new JspPropertyDefinition();
    }

    protected void end(String body) {
        JspProcessorDefinition processor = _parent.getProcessorDefinition();
        processor.addPropertyDefinitiion(_property);
        _property.setProcessorDefinition(processor);
    }

    private class RequiredSetter extends TagHandler {
        
        private AttributeTagHandler _parent;
        
        private RequiredSetter(AttributeTagHandler parent) {
            _parent = parent;
        }
        
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
        
    }
    
}

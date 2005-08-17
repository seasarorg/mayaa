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
package org.seasar.maya.impl.jsp.builder.library.handler;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.jsp.builder.library.JspProcessorDefinition;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * tldのtagタグ用ハンドラ.
 * @author suga
 */
public class TagTagHandler extends TagHandler {

    private static final Log LOG = LogFactory.getLog(TagTagHandler.class);
    
    private TaglibTagHandler _parent;
    private JspProcessorDefinition _processor;
    
    public TagTagHandler(TaglibTagHandler parent) {
        _parent = parent;
        putHandler("attribute", new AttributeTagHandler(this));
        putHandler("variable", new VariableTagHandler(this));
        putHandler("name", new TagHandler() {
            protected void end(String body) {
                _processor.setName(body);
            }
        });
        putHandler("tag-class", new TagClassSetter(this));
        putHandler("tei-class", new TeiClassSetter(this));
        // JSP1.1
        putHandler("tagclass", new TagClassSetter(this));
        putHandler("teiclass", new TeiClassSetter(this));
    }

    protected void start(Attributes attributes) {
        _processor = new JspProcessorDefinition();
    }

    protected void end(String body) {
        _parent.getLibraryDefinition().addProcessorDefinition(_processor);
        _processor = null;
    }
    
    public JspProcessorDefinition getProcessorDefinition() {
        if(_processor == null) {
            throw new IllegalStateException();
        }
        return _processor;
    }

    private class TagClassSetter extends TagHandler {

        private TagTagHandler _parent;
        
        private TagClassSetter(TagTagHandler parent) {
            _parent = parent;
        }
        
        protected void end(String body) {
			try {
                Class clazz = ObjectUtil.loadClass(body, Tag.class);
                _processor.setTagClass(clazz);
            } catch (RuntimeException e) {
                if(LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _parent.invalidate();
            }
        }
    
    }
    
    private class TeiClassSetter extends TagHandler {

        private TagTagHandler _parent;
        
        private TeiClassSetter(TagTagHandler parent) {
            _parent = parent;
        }
        
        protected void end(String body) {
			try {
                Class clazz = ObjectUtil.loadClass(body, TagExtraInfo.class);
                TagExtraInfo tei = (TagExtraInfo)ObjectUtil.newInstance(clazz);
                _processor.setTEI(tei);
            } catch (RuntimeException e) {
                if(LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _parent.invalidate();
            }
        }
        
    }
    
}

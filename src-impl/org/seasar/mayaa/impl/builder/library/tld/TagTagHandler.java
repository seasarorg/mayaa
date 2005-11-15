/*
 * Copyright 2004-2005 the Seasar Foundation and the Others.
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
package org.seasar.maya.impl.builder.library.tld;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagExtraInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.impl.builder.library.TLDLibraryDefinition;
import org.seasar.maya.impl.builder.library.TLDProcessorDefinition;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TagTagHandler extends TagHandler {

    protected static final Log LOG =
            LogFactory.getLog(TagTagHandler.class);

    private TaglibTagHandler _parent;
    private TLDProcessorDefinition _processor;

    public TagTagHandler(TaglibTagHandler parent) {
        super("tag");
        _parent = parent;
        putHandler(new TeiTagHandler(this));
        putHandler(new AttributeTagHandler(this));
        putHandler(new TagHandler("name") {
            protected void end(String body) {
                setProcessorName(body);
            }
        });
        putHandler(new TagClassSetter("tag-class", this));
        putHandler(new TagClassSetter("tagclass", this));
        putHandler(new TeiClassSetter("tei-class", this));
        putHandler(new TeiClassSetter("teiclass", this));
    }

    protected void setProcessorName(String name) {
        _processor.setName(name);
    }

    protected void setProcessorClass(Class clazz) {
        _processor.setProcessorClass(clazz);
    }

    protected void setTeiClass(Class teiClass) {
        _processor.setExtraInfoClass(teiClass);
    }

    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _processor = new TLDProcessorDefinition();
        _processor.setSystemID(systemID);
        _processor.setLineNumber(lineNumber);
    }

    protected void end(String body) {
        TLDLibraryDefinition library = _parent.getLibraryDefinition();
        library.addProcessorDefinition(_processor);
        _processor.setLibraryDefinition(library);
        _processor = null;
    }

    public TLDProcessorDefinition getProcessorDefinition() {
        if(_processor == null) {
            throw new IllegalStateException();
        }
        return _processor;
    }

    private class TagClassSetter extends TagHandler {

        private TagTagHandler _handler;

        private TagClassSetter(String name, TagTagHandler handler) {
            super(name);
            _handler = handler;
        }

        protected void end(String body) {
            try {
                Class clazz = ObjectUtil.loadClass(body, Tag.class);
                setProcessorClass(clazz);
            } catch (RuntimeException e) {
                if(LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _handler.invalidate();
            }
        }

    }

    private class TeiClassSetter extends TagHandler {

        private TagTagHandler _handler;

        private TeiClassSetter(String name, TagTagHandler handler) {
            super(name);
            _handler = handler;
        }

        protected void end(String body) {
            try {
                Class clazz = ObjectUtil.loadClass(body, TagExtraInfo.class);
                setTeiClass(clazz);
            } catch (RuntimeException e) {
                if(LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _handler.invalidate();
            }
        }

    }

}
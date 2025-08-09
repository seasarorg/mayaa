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
package org.seasar.mayaa.impl.builder.library.tld;

import jakarta.servlet.jsp.tagext.Tag;
import jakarta.servlet.jsp.tagext.TagExtraInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.builder.library.TLDLibraryDefinition;
import org.seasar.mayaa.impl.builder.library.TLDProcessorDefinition;
import org.seasar.mayaa.impl.engine.processor.JspProcessor;
import org.seasar.mayaa.impl.util.IllegalClassTypeException;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
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
        putHandler(new TagHandler("body-content") {
            protected void end(String body) {
                setBodyContent(body);
            }
        });
        putHandler(new TagHandler("bodycontent") {
            protected void end(String body) {
                setBodyContent(body);
            }
        });
        putHandler(new TagHandler("dynamic-attributes") {
            protected void end(String body) {
                try {
                    getProcessorDefinition().setDynamicAttribute(
                            ObjectUtil.booleanValue(body, false));
                } catch (RuntimeException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    invalidate();
                }
            }
        });
    }

    protected void setProcessorName(String name) {
        _processor.setName(name);
    }

    protected void setProcessorClass(Class<?> clazz) {
        _processor.setProcessorClass(clazz);
    }

    protected void setTeiClass(Class<?> teiClass) {
        _processor.setExtraInfoClass(teiClass);
    }

    protected void setBodyContent(String bodyContent) {
        _processor.setBodyContent(bodyContent);
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
        if (_processor == null) {
            throw new IllegalStateException();
        }
        return _processor;
    }

    private class TagClassSetter extends TagHandler {

        private TagTagHandler _handler;

        protected TagClassSetter(String name, TagTagHandler handler) {
            super(name);
            _handler = handler;
        }

        protected void end(String body) {
            try {
                Class<?> tagClass = ObjectUtil.loadClass(body);
                if (JspProcessor.isSupportClass(tagClass)) {
                    setProcessorClass(tagClass);
                } else {
                    /* Tag / SimpleTag */
                    throw new IllegalClassTypeException(Tag.class, tagClass);
                }
            } catch (RuntimeException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                _handler.invalidate();
            }
        }

    }

    private class TeiClassSetter extends TagHandler {

        private TagTagHandler _handler;

        protected TeiClassSetter(String name, TagTagHandler handler) {
            super(name);
            _handler = handler;
        }

        protected void end(String body) {
            try {
                Class<?> clazz = ObjectUtil.loadClass(body, TagExtraInfo.class);
                setTeiClass(clazz);
            } catch (RuntimeException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _handler.invalidate();
            }
        }

    }

}

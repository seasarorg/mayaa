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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.builder.library.TLDProcessorDefinition;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.xml.TagHandler;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class TeiTagHandler extends TagHandler {

    protected static final Log LOG =
            LogFactory.getLog(TeiTagHandler.class);

    private TagTagHandler _parent;
    private Class<?> _teiClass;

    public TeiTagHandler(TagTagHandler parent) {
        super("tei");
        _parent = parent;
        putHandler(new TeiClassSetter("tei-class", this));
        putHandler(new TeiClassSetter("teiclass", this));
    }

    protected void invalidateParent() {
        _parent.invalidate();
    }

    protected void end(String body) {
        if (_teiClass != null) {
            TLDProcessorDefinition processor = _parent.getProcessorDefinition();
            processor.setExtraInfoClass(_teiClass);
        }
    }

    protected void setTeiClass(Class<?> teiClass) {
        _teiClass = teiClass;
    }

    private class TeiClassSetter extends TagHandler {

        private TeiTagHandler _handler;

        protected TeiClassSetter(String name, TeiTagHandler handler) {
            super(name);
            _handler = handler;
        }

        protected void end(String body) {
            try {
                Class<?> clazz = ObjectUtil.loadClass(body, Tag.class);
                setTeiClass(clazz);
            } catch (RuntimeException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage());
                }
                _handler.invalidate();
                _handler.invalidateParent();
            }
        }

    }

}

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.impl.builder.library.TLDProcessorDefinition;
import org.seasar.mayaa.impl.builder.library.TLDPropertyDefinition;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.xml.TagHandler;
import org.xml.sax.Attributes;

/**
 * @author Koji Suga (Gluegent, Inc.)
 * @author Hisayoshi Sasaki (Gluegent, Inc.)
 */
public class AttributeTagHandler extends TagHandler {

    protected static final Log LOG =
            LogFactory.getLog(AttributeTagHandler.class);

    private TLDPropertyDefinition _property;
    private TagTagHandler _parent;

    public AttributeTagHandler(TagTagHandler parent) {
        super("attribute");
        _parent = parent;
        putHandler(new TagHandler("name") {
            protected void end(String body) {
                getProperty().setName(body);
            }
        });
        putHandler(new TagHandler("required") {
            protected void end(String body) {
                try {
                    getProperty().setRequired(ObjectUtil.booleanValue(body, false));
                } catch (RuntimeException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    invalidateParent();
                }
            }
        });
        putHandler(new TagHandler("rtexprvalue") {
            protected void end(String body) {
                try {
                    getProperty().setRtexprvalue(ObjectUtil.booleanValue(body, false));
                } catch (RuntimeException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    invalidateParent();
                }
            }
        });
        putHandler(new TagHandler("type") {
            protected void end(String body) {
                try {
                    Class expectedClass = ObjectUtil.loadClass(body);
                    getProperty().setExpectedClass(expectedClass);
                } catch (RuntimeException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(e.getMessage(), e);
                    }
                    invalidateParent();
                }
            }
        });
    }

    protected TLDPropertyDefinition getProperty() {
        return _property;
    }

    protected void setProperty(TLDPropertyDefinition property) {
        _property = property;
    }

    protected void invalidateParent() {
        _parent.invalidate();
    }

    protected void start(
            Attributes attributes, String systemID, int lineNumber) {
        TLDPropertyDefinition property = new TLDPropertyDefinition();
        property.setSystemID(systemID);
        property.setLineNumber(lineNumber);
        setProperty(property);
    }

    protected void end(String body) {
        TLDProcessorDefinition processor = _parent.getProcessorDefinition();
        getProperty().setPropertySet(_parent.getProcessorDefinition());
        processor.addPropertyDefinitiion(getProperty());
    }

}

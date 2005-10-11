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
package org.seasar.maya.impl.source.factory;

import org.seasar.maya.impl.provider.factory.AbstractParameterizableTagHandler;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.provider.Parameterizable;
import org.seasar.maya.source.factory.SourceFactory;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SourceFactoryTagHandler
        extends AbstractParameterizableTagHandler {

    private SourceFactory _sourceFactory;
    
    public SourceFactoryTagHandler() {
        super("pageSourceDescriptor");
    }

    protected void start(
    		Attributes attributes, String systemID, int lineNumber) {
        _sourceFactory = (SourceFactory)XMLUtil.getObjectValue(
                attributes, "class", null, SourceFactory.class);
    }

    public SourceFactory getSourceFactory() {
        if(_sourceFactory == null) {
            throw new IllegalStateException();
        }
        return _sourceFactory;
    }
    
    public Parameterizable getParameterizable() {
        return getSourceFactory();
    }

}

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
package org.seasar.mayaa.impl.builder;

import java.io.InputStream;

import org.seasar.mayaa.builder.SpecificationBuilder;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.xml.XMLReaderPool;
import org.seasar.mayaa.source.SourceDescriptor;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class SpecificationBuilderImpl extends ParameterAwareImpl
        implements SpecificationBuilder, CONST_IMPL {

    private static final long serialVersionUID = -1272395705148798946L;

    private boolean _outputMayaaWhitespace = false;

    protected XMLReaderPool getXMLReaderPool(String systemID) {
        return XMLReaderPool.getPool();
    }

    protected SpecificationNodeHandler createContentHandler(
            Specification specification) {
        SpecificationNodeHandler handler =
            new SpecificationNodeHandler(specification);
        handler.setOutputMayaaWhitespace(_outputMayaaWhitespace);
        return handler;
    }

    protected String getPublicID() {
        return URI_MAYAA + "/specification";
    }

    protected void afterBuild(Specification specification) {
        // for TemplateBuilderImpl
    }

    public void build(Specification specification) {
        if (specification == null) {
            throw new IllegalArgumentException();
        }
        SourceDescriptor source = specification.getSource();
        if (source.exists()) {
            SpecificationNodeHandler handler =
                createContentHandler(specification);

            XMLReaderPool pool = getXMLReaderPool(source.getSystemID());
            XMLReader xmlReader =
                pool.borrowXMLReader(handler, true, false, false);
            InputStream stream = source.getInputStream();
            InputSource input = new InputSource(stream);
            input.setPublicId(getPublicID());
            input.setSystemId(source.getSystemID());
            try {
                xmlReader.parse(input);
                afterBuild(specification);
            } catch (Throwable t) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new RuntimeException(
                        "build failed. " + source.getSystemID(), t);
            } finally {
                pool.returnXMLReader(xmlReader);
                IOUtil.close(stream);
            }
        }
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if ("outputMayaaWhitespace".equals(name)) {
            _outputMayaaWhitespace = ObjectUtil.booleanValue(value, true);
        }
        super.setParameter(name, value);
    }

}

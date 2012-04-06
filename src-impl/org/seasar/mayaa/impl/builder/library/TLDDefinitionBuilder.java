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
package org.seasar.mayaa.impl.builder.library;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.library.DefinitionBuilder;
import org.seasar.mayaa.builder.library.LibraryDefinition;
import org.seasar.mayaa.engine.specification.URI;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.builder.library.scanner.SourceAlias;
import org.seasar.mayaa.impl.builder.library.scanner.WebXMLTaglibSourceScanner;
import org.seasar.mayaa.impl.builder.library.tld.TLDLibraryDefinitionHandler;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDDefinitionBuilder extends ParameterAwareImpl
        implements DefinitionBuilder {

    private static final long serialVersionUID = 8241504208792699894L;
    private static final Log LOG = LogFactory.getLog(TLDDefinitionBuilder.class);

    public LibraryDefinition build(SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        if (source.exists() && systemID.toLowerCase().endsWith(".tld")) {
            InputStream stream = source.getInputStream();
            TLDLibraryDefinitionHandler handler = new TLDLibraryDefinitionHandler();
            try {
                XMLUtil.parse(handler, stream, "tld", systemID, true, true, true);
            } catch (Throwable t) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("TLD parse error on " + systemID, t);
                }
                return null;
            } finally {
                IOUtil.close(stream);
            }
            LibraryDefinition library = handler.getLibraryDefinition();
            boolean assigned = ObjectUtil.booleanValue(source.getParameter(
                    WebXMLTaglibSourceScanner.ASSIGNED), false);
            if (assigned || "/META-INF/taglib.tld".equals(systemID)) {
                URI assignedURI = SpecificationUtil.createURI(
                        source.getParameter(SourceAlias.ALIAS));
                library.addAssignedURI(assignedURI);
            }
            return library;
        }
        return null;
    }

}

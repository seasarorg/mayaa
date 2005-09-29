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

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.maya.builder.library.DefinitionBuilder;
import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.impl.builder.library.scanner.SourceAlias;
import org.seasar.maya.impl.builder.library.scanner.WebXMLTaglibSourceScanner;
import org.seasar.maya.impl.provider.UnsupportedParameterException;
import org.seasar.maya.impl.util.IOUtil;
import org.seasar.maya.impl.util.ObjectUtil;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDDefinitionBuilder implements DefinitionBuilder {

    private static Log LOG = LogFactory.getLog(TLDDefinitionBuilder.class);

    public void setParameter(String name, String value) {
        throw new UnsupportedParameterException(getClass(), name);
    }

    public LibraryDefinition build(SourceDescriptor source) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        String systemID = source.getSystemID();
        if(source.exists() && systemID.toLowerCase().endsWith(".tld")) {
            InputStream stream = source.getInputStream();
            TLDHandler handler = new TLDHandler();
            try {
                XMLUtil.parse(handler, stream, "tld", systemID, true, true, true);
            } catch(Throwable t) {
                if(LOG.isErrorEnabled()) {
                    LOG.error("TLD parse error on " + systemID, t);
                }
                return null;
            } finally {
                IOUtil.close(stream);
            }
            TLDLibraryDefinition library = handler.getLibraryDefinition();
            boolean assigned = ObjectUtil.booleanValue(source.getAttribute(
                    WebXMLTaglibSourceScanner.ASSIGNED), false);
            if(assigned || "/META-INF/taglib.tld".equals(systemID)) {
                library.addAssignedURI(source.getAttribute(SourceAlias.ALIAS));
            }
            return library;
        }
        return null;
    }

}

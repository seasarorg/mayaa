/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.jsp.builder.library;

import java.io.InputStream;

import org.seasar.maya.builder.library.LibraryDefinition;
import org.seasar.maya.builder.library.LibraryDefinitionBuilder;
import org.seasar.maya.impl.jsp.builder.library.handler.TLDHandler;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.impl.util.XmlUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TLDLibraryDefinitionBuilder implements LibraryDefinitionBuilder {

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    private boolean isMLD(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        return path.toLowerCase().endsWith(".mld");
    }
    
    public LibraryDefinition build(SourceDescriptor source) {
        if(source == null) {
            throw new IllegalArgumentException();
        }
        if(source.exists() && isMLD(source.getPath())) {
            InputStream stream = source.getInputStream();
            String systemID = source.getSystemID();
            TLDHandler handler = new TLDHandler(systemID);
            // FIXME validation="true" ÇæÇ∆ÅAJSTLÇÃc.tld/x.tldÇ»Ç«XSDÇóòópÇ∑ÇÈÇ‡ÇÃÇ≈SAXó·äO
            XmlUtil.parse(handler, stream, "tld", systemID, true, false, false);
            return handler.getLibraryDefinition();
        }
        return null;
    }
    
}

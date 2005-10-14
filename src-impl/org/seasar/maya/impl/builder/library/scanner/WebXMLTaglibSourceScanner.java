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
package org.seasar.maya.impl.builder.library.scanner;

import java.io.InputStream;
import java.util.Iterator;

import org.seasar.maya.builder.library.scanner.SourceScanner;
import org.seasar.maya.impl.ParameterAwareImpl;
import org.seasar.maya.impl.source.ApplicationSourceDescriptor;
import org.seasar.maya.impl.util.IOUtil;
import org.seasar.maya.impl.util.XMLUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class WebXMLTaglibSourceScanner extends ParameterAwareImpl
		implements SourceScanner {

    public static final String ASSIGNED =
        WebXMLTaglibSourceScanner.class + ".ASSIGNED";

    protected Iterator scanWebXml(SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        if (source.exists() == false) {
            throw new IllegalStateException();
        }
        InputStream stream = source.getInputStream();
        try {
            TaglibLocationsHandler handler = new TaglibLocationsHandler();
            XMLUtil.parse(handler, stream, "web.xml",
                    source.getSystemID(), true, true, true);
            return handler.iterateTaglibLocations();
        } finally {
            IOUtil.close(stream);
        }
    }

    public Iterator scan() {
        ApplicationSourceDescriptor source =
            new ApplicationSourceDescriptor();
        source.setRoot(ApplicationSourceDescriptor.WEB_INF);
        source.setSystemID("web.xml");
        return new TaglibLocationIterator(scanWebXml(source));
    }

    private class TaglibLocationIterator implements Iterator {

        private Iterator _it;

        private TaglibLocationIterator(Iterator it) {
            if (it == null) {
                throw new IllegalArgumentException();
            }
            _it = it;
        }

        public boolean hasNext() {
            return _it.hasNext();
        }

        public Object next() {
            SourceAlias alias = (SourceAlias) _it.next();
            String systemID = alias.getSystemID();
            if (systemID.startsWith("/WEB-INF/")) {
                systemID = systemID.substring(9);
            }
            ApplicationSourceDescriptor source =
                new ApplicationSourceDescriptor();
            if (systemID.startsWith("/") == false) {
                source.setRoot(ApplicationSourceDescriptor.WEB_INF);
            }
            source.setSystemID(systemID);
            source.setParameter(SourceAlias.ALIAS, alias.getAlias());
            source.setParameter(ASSIGNED, "true");
            return source;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

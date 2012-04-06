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
package org.seasar.mayaa.impl.builder.library.scanner;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.builder.library.scanner.SourceScanner;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.source.ApplicationSourceDescriptor;
import org.seasar.mayaa.impl.util.IOUtil;
import org.seasar.mayaa.impl.util.IteratorUtil;
import org.seasar.mayaa.impl.util.StringUtil;
import org.seasar.mayaa.impl.util.XMLUtil;
import org.seasar.mayaa.source.SourceDescriptor;

/**
 * web.xmlのtaglibディレクティブを参照するためのクラス。
 *
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author Koji Suga (Gluegent Inc.)
 */
public class WebXMLTaglibSourceScanner extends ParameterAwareImpl
        implements SourceScanner {

    private static final long serialVersionUID = -4740935373481152275L;
    private static final Log LOG = LogFactory.getLog(WebXMLTaglibSourceScanner.class);

    public static final String ASSIGNED =
        WebXMLTaglibSourceScanner.class + ".ASSIGNED";
    public static final String REAL_PATH =
        WebXMLTaglibSourceScanner.class + ".REAL_PATH";

    /**
     * web.xmlをパースし、&lt;taglib-uri&gt;と&lt;taglib-location&gt;から作成した
     * {@link SourceDescriptor}のIteratorを返します。(nullにはなりません)
     * {@link SourceAlias}を元にしたaliasパラメータがセットされるものもあります。
     *
     * @param source web.xmlのSourceDescriptor
     * @return SourceAliasのIterator
     */
    protected Iterator scanWebXml(SourceDescriptor source) {
        if (source == null) {
            throw new IllegalArgumentException();
        }
        if (source.exists() == false) {
            if (LOG.isInfoEnabled()) {
                LOG.info(StringUtil.getMessage(
                        WebXMLTaglibSourceScanner.class, 0, source.getSystemID()));
            }
            return IteratorUtil.NULL_ITERATOR;
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
        SourceDescriptor source = FactoryFactory.getBootstrapSource(
                ApplicationSourceDescriptor.WEB_INF, "web.xml");
        return new TaglibLocationIterator(scanWebXml(source));
    }

    /**
     * {@link SourceAlias}のIteratorを{@link SourceDescriptor}のIteratorとして
     * 扱うためのクラス。
     *
     * @author Koji Suga (Gluegent Inc.)
     */
    private static class TaglibLocationIterator implements Iterator {

        private Iterator _it;

        protected TaglibLocationIterator(Iterator it) {
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
                systemID = systemID.substring("/WEB-INF/".length());
            }
            ApplicationSourceDescriptor source =
                new ApplicationSourceDescriptor();
            if (systemID.startsWith("/") == false) {
                source.setRoot(ApplicationSourceDescriptor.WEB_INF);
            }
            source.setSystemID(systemID);
            source.setParameter(SourceAlias.ALIAS, alias.getAlias());
            source.setParameter(REAL_PATH, alias.getSystemID());
            source.setParameter(ASSIGNED, "true");
            return source;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}

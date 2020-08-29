/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.impl.source.ClassLoaderSourceDescriptor;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class WebXmlAliasSourceScannerTest {
    @Before
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        scanner = new WebXMLTaglibSourceScanner();
    }

    @After
    public void tearDown() throws Exception {
        ManualProviderFactory.tearDown();
    }

    private WebXMLTaglibSourceScanner scanner;

    @Test
    public void testScan() throws Exception {
        ClassLoaderSourceDescriptor webinfSource =
            new ClassLoaderSourceDescriptor();
        webinfSource.setRoot(
                "org/seasar/mayaa/impl/builder/library/scanner/WEB-INF");
        webinfSource.setSystemID("web.xml");
        ManualProviderFactory.MOCK_INSTANCE.setBootstrapSource(webinfSource);

        List<String> sources = new ArrayList<>();
        for(Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext(); ) {
            SourceDescriptor source = (SourceDescriptor)it.next();
            sources.add(source.getSystemID());
        }

        assertTrue("1", sources.contains("/tlds/mayaa-sample2.tld"));
        assertTrue("2", sources.contains("/tlds/mayaa-sample3.tld"));

        assertEquals("3", 2, sources.size());
    }

    @Test
    public void testScan24() throws Exception {
        ClassLoaderSourceDescriptor webinfSource =
            new ClassLoaderSourceDescriptor();
        webinfSource.setRoot(
                "org/seasar/mayaa/impl/builder/library/scanner/WEB-INF24");
        webinfSource.setSystemID("web.xml");
        ManualProviderFactory.MOCK_INSTANCE.setBootstrapSource(webinfSource);

        List<String> sources = new ArrayList<>();
        for(Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext(); ) {
            SourceDescriptor source = (SourceDescriptor)it.next();
            sources.add(source.getSystemID());
        }

        assertTrue("1", sources.contains("/tlds/mayaa-sample2.tld"));
        assertTrue("2", sources.contains("/tlds/mayaa-sample3.tld"));

        assertEquals("3", 2, sources.size());
    }

}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Koji Suga (Gluegent, Inc.)
 */
public class FolderSourceScannerTest {
    @Before
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        scanner = new FolderSourceScanner();
    }

    @After
    public void tearDown() throws Exception {
        ManualProviderFactory.tearDown();
    }

    private FolderSourceScanner scanner;

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScan() {
        scanner.setParameter("folder", "/WEB-INF");
        scanner.setParameter("recursive", "false");
        scanner.setParameter("extension", ".xml");
        scanner.setParameter("extension", ".tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            sources.add(source.getSystemID());
        }

        System.out.println(sources);
        assertTrue("1", sources.contains("/web.xml"));
        assertFalse("2", sources.contains("/tlds"));
        assertFalse("3", sources.contains("/tlds/TaglibSAXParser11Test.tld"));

        assertEquals("4", 1, sources.size());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanRecursive() {
        scanner.setParameter("folder", "/WEB-INF");
        scanner.setParameter("recursive", "true");
        scanner.setParameter("extension", ".xml");
        scanner.setParameter("extension", ".tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            sources.add(source.getSystemID());
        }

        assertTrue("1", sources.contains("/web.xml"));
        assertFalse("2", sources.contains("/tlds"));
        assertTrue("3", sources.contains("/tlds/TaglibSAXParser11Test.tld"));

        assertEquals("4", 5, sources.size());
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanExtension() {
        scanner.setParameter("folder", "/WEB-INF");
        scanner.setParameter("recursive", "true");
        scanner.setParameter("extension", ".mld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            sources.add(source.getSystemID());
        }

        assertTrue("1", sources.contains("/mlds/TestMLD.mld"));

        assertEquals("2", 1, sources.size());
    }
}

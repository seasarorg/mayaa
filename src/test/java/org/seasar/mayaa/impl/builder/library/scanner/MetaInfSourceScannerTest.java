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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.source.HavingAliasSourceDescriptor;
import org.seasar.mayaa.source.SourceDescriptor;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Mitsutaka Watanabe
 */
public class MetaInfSourceScannerTest {

    @BeforeEach
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        scanner = new MetaInfSourceScanner();
    }

    @AfterEach
    public void tearDown() throws Exception {
        ManualProviderFactory.tearDown();
    }

    private MetaInfSourceScanner scanner;

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanDefault() {
        scanner.setParameter("folder", "/WEB-INF/lib");
        scanner.setParameter("extension", ".jar");
        scanner.setParameter("jar.extension", ".mld");
        scanner.setParameter("jar.extension", ".tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + ":" + name;
            }
            sources.add(name);
        }

        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fn.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/permittedTaglibs.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/scriptfree.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest2.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest3.tld"), "entry not match");
        assertEquals(18, sources.size(), "size not match");
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanRecursive() {
        scanner.setParameter("folder", "/WEB-INF/lib");
        scanner.setParameter("extension", ".jar");
        scanner.setParameter("jar.extension", ".mld");
        scanner.setParameter("jar.extension", ".tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + ":" + name;
            }
            sources.add(name);
        }

        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fn.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/permittedTaglibs.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/scriptfree.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/sql.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/x.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest2.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest3.tld"), "entry not match");
        assertEquals(18, sources.size(), "size not match");
    }
    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanIgnore() {
        scanner.setParameter("folder", "/WEB-INF/lib");
        scanner.setParameter("extension", ".jar");
        scanner.setParameter("ignore", "standard-");
        scanner.setParameter("jar.extension", ".mld");
        scanner.setParameter("jar.extension", ".tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + ":" + name;
            }
            sources.add(name);
        }

        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest2.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest3.tld"), "entry not match");
        assertEquals(3, sources.size(), "size not match");
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanJarIgnore() {
        scanner.setParameter("folder", "/WEB-INF/lib");
        scanner.setParameter("extension", ".jar");
        scanner.setParameter("jar.extension", ".mld");
        scanner.setParameter("jar.extension", ".tld");
        scanner.setParameter("jar.ignore", "META-INF/x");
        scanner.setParameter("jar.ignore", "META-INF/sql");
        scanner.setParameter("jar.ignore", "META-INF/scriptfree");
        scanner.setParameter("jar.ignore", "META-INF/fn");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + ":" + name;
            }
            sources.add(name);
        }

        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/c.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0-rt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt-1_0.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/fmt.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/standard-1.1.2.jar:/META-INF/permittedTaglibs.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest2.tld"), "entry not match");
        assertTrue(sources.contains("/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar:/META-INF/TagLibraryInfoManagerImplTest3.tld"), "entry not match");
        assertEquals(10, sources.size(), "size not match");
    }
}

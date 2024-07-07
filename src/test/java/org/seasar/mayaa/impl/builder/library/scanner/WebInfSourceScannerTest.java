/*
 * Copyright 2004-2024 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http!//www.apache.org/licenses/LICENSE-2.0
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
public class WebInfSourceScannerTest {

    @BeforeEach
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();

        scanner = new WebInfSourceScanner();
    }

    @AfterEach
    public void tearDown() throws Exception {
        ManualProviderFactory.tearDown();
    }

    private WebInfSourceScanner scanner;

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanDefault() {

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + "!" + name;
            }
            sources.add(name);
        }

        String expectedList[] = {
            "/WEB-INF/mlds/TestMLD.mld",
            "/WEB-INF/tlds/TaglibSAXParser11Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser12Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser11_errorTest.tld",
            "/WEB-INF/tlds/TaglibSAXParser20Test.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fn.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/permittedTaglibs.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/scriptfree.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/sql-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/sql-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/sql.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/x-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/x-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/x.tld",
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest.tld",
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest2.tld",
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest3.tld",    
        };
        for (String expected: expectedList) {
            assertTrue(sources.contains(expected), "not contains " + expected);
        }
        assertEquals(expectedList.length, sources.size(), "size not match");
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanIgnore() {
        scanner.setParameter("exclude", "tlds/*.tld");
        scanner.setParameter("includeJar", "TagLibraryInfoManagerImplTest.jar");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + "!" + name;
            }
            sources.add(name);
        }

        String expectedList[] = {
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest.tld",
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest2.tld",
            "/WEB-INF/lib/TagLibraryInfoManagerImplTest.jar!/META-INF/TagLibraryInfoManagerImplTest3.tld",    
        };
        for (String expected: expectedList) {
            assertTrue(sources.contains(expected), "not contains " + expected);
        }
        assertEquals(expectedList.length, sources.size(), "size not match");
    }

    /*
     * Test method for 'org.seasar.mayaa.impl.builder.library.scanner.FolderSourceScanner.scan()'
     */
    @Test
    public void testScanJarIgnore() {
        scanner.setParameter("include", "tlds/*.tld");
        scanner.setParameter("includeJar", "standard-*.jar");
        scanner.setParameter("excludeInJarMetaInf", "{x,sql,scriptfree,fn}*");
        scanner.setParameter("includeInJarMetaInf", "*.tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + "!" + name;
            }
            sources.add(name);
        }

        System.out.println(sources);
        String expectedList[] = {
            "/WEB-INF/tlds/TaglibSAXParser11Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser12Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser11_errorTest.tld",
            "/WEB-INF/tlds/TaglibSAXParser20Test.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/permittedTaglibs.tld",
        };
        for (String expected: expectedList) {
            assertTrue(sources.contains(expected), "not contains " + expected);
        }
        assertEquals(expectedList.length, sources.size(), "size not match");
    }

    @Test
    public void testScanJarIgnore2() {
        scanner.setParameter("include", "**/*.tld");
        scanner.setParameter("excludeJar", "TagLibraryInfoManagerImplTest.jar");
        scanner.setParameter("includeJar", "*.jar");
        scanner.setParameter("excludeInJarMetaInf", "{x,sql,scriptfree,fn}*");
        scanner.setParameter("includeInJarMetaInf", "*.tld");

        List<String> sources = new ArrayList<>();
        for (Iterator<SourceDescriptor> it = scanner.scan(); it.hasNext();) {
            SourceDescriptor source = (SourceDescriptor) it.next();
            String name = source.getSystemID();
            if (source instanceof HavingAliasSourceDescriptor) {
                name = ((HavingAliasSourceDescriptor) source).getAlias().getAlias() + "!" + name;
            }
            sources.add(name);
        }

        System.out.println(sources);
        String expectedList[] = {
            "/WEB-INF/tlds/TaglibSAXParser11Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser12Test.tld",
            "/WEB-INF/tlds/TaglibSAXParser11_errorTest.tld",
            "/WEB-INF/tlds/TaglibSAXParser20Test.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/c.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0-rt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt-1_0.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/fmt.tld",
            "/WEB-INF/lib/standard-1.1.2.jar!/META-INF/permittedTaglibs.tld",
        };
        for (String expected: expectedList) {
            assertTrue(sources.contains(expected), "not contains " + expected);
        }
        assertEquals(expectedList.length, sources.size(), "size not match");
    }
}

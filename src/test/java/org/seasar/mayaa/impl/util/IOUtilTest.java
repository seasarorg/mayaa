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
package org.seasar.mayaa.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class IOUtilTest {

    private String value =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<m:mayaa xmlns:m=\"http://mayaa.seasar.org\">\n\n        <m:write m:id=\"message\" value=\"日本語\" />\n\n</m:mayaa>\n";
    private String encoding = "UTF-8";

    @Before
    public void setUp() throws Exception {
        ManualProviderFactory.setUp(this);
        ManualProviderFactory.SCRIPT_ENVIRONMENT.initScope();
    }

    @After
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#readStream(java.io.InputStream, java.lang.String)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testReadStream() throws Throwable {
        ByteArrayInputStream is =
            new ByteArrayInputStream(value.getBytes(encoding));

        String read = IOUtil.readStream(is, encoding);

        assertEquals(value, read);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#writeStream(java.io.OutputStream, java.lang.String, java.lang.String)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testWriteStream() throws Throwable {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        IOUtil.writeStream(os, value, encoding);

        assertEquals(value, os.toString(encoding));
    }

    // 空白ありのファイル名
    private static final String TESTFILE_PATH = "org/seasar/mayaa/impl/util/IOUtilTest da@ta.txt";
    private static final String TESTFILE_NAME = "IOUtilTest da@ta.txt";

    private ClassLoader getLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#openStream(java.net.URL)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testOpenStream() throws Throwable {
        URL url = getLoader().getResource(TESTFILE_PATH);
        assertNotNull(url);
        InputStream is = IOUtil.openStream(url);
        assertNotNull(is);
        assertEquals("foo", IOUtil.readStream(is, "UTF-8"));

        InputStream nullIS = IOUtil.openStream(null);
        assertNull(nullIS);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getLastModified(java.net.URL)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetLastModified() throws Throwable {
        URL url = getLoader().getResource(TESTFILE_PATH);
        assertNotNull(url);

        long fileLastModified = IOUtil.getLastModified(url);
        File testFile = new File(URLDecoder.decode(url.toString().substring(5), "UTF-8"));
        assertEquals(testFile.lastModified(), fileLastModified);

        URL urlInJar = getLoader().getResource("junit/framework/TestCase.class");
        long jarLastModified = IOUtil.getLastModified(urlInJar);
        assertEquals(CONST_IMPL.NULL_DATE_MILLIS, jarLastModified);

        long notFound = IOUtil.getLastModified(null);
        assertEquals(CONST_IMPL.NULL_DATE_MILLIS, notFound);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getFile(java.net.URL)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetFile() throws Throwable {
        URL expected = getLoader().getResource(TESTFILE_PATH);
        File actual = IOUtil.getFile(expected);
        InputStream expectedIS = null;
        InputStream actualIS = null;
        try {
            expectedIS = IOUtil.openStream(expected);
            actualIS = new FileInputStream(actual);
            String expectedString = IOUtil.readStream(expectedIS, encoding);
            String actualString = IOUtil.readStream(actualIS, encoding);
            assertEquals(expectedString, actualString);
        } finally {
            IOUtil.close(expectedIS);
            IOUtil.close(actualIS);
        }
        assertEquals(expected, actual.toURI().toURL());
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResource(java.lang.String)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResource() throws Throwable {
        URL expected = getLoader().getResource(TESTFILE_PATH);
        URL actual = IOUtil.getResource(TESTFILE_PATH);
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResourceAsStream(java.lang.String)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResourceAsStream() throws Throwable {
        URL url = IOUtil.getResource(TESTFILE_PATH);
        assertNotNull(url);
        InputStream is = IOUtil.openStream(url);
        assertNotNull(is);
        assertEquals("foo", IOUtil.readStream(is, "UTF-8"));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#resolveName(java.lang.Class, java.lang.String)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testResolveName() throws Throwable {
        assertEquals("org/seasar/mayaa/foo.txt",
                IOUtil.resolveName(FactoryFactory.class, "foo.txt"));
        assertEquals("foo.txt",
                IOUtil.resolveName(FactoryFactory.class, "/foo.txt"));
        assertNull(IOUtil.resolveName(FactoryFactory.class, null));

        assertEquals("java/lang/foo.txt",
                IOUtil.resolveName(new String[0].getClass(), "foo.txt"));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResource(java.lang.String, java.lang.Class)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResourceNeighbor() throws Throwable {
        URL expected = getLoader().getResource(TESTFILE_PATH);
        URL actual = IOUtil.getResource(TESTFILE_NAME, getClass());
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResourceAsStream(java.lang.String, java.lang.Class)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResourceAsStreamNeighbor() throws Throwable {
        InputStream is = IOUtil.getResourceAsStream(TESTFILE_NAME, getClass());
        assertNotNull(is);
        assertEquals("foo", IOUtil.readStream(is, "UTF-8"));
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResource(java.lang.String, java.lang.ClassLoader)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResourceClassLoader() throws Throwable {
        URL expected = getLoader().getResource(TESTFILE_PATH);
        URL actual = IOUtil.getResource(TESTFILE_PATH, getClass().getClassLoader());
        assertEquals(expected, actual);
    }

    /**
     * Test method for {@link org.seasar.mayaa.impl.util.IOUtil#getResourceAsStream(java.lang.String, java.lang.ClassLoader)}.
     * @throws Throwable if occur.
     */
    @Test
    public void testGetResourceAsStreamClassLoader() throws Throwable {
        InputStream is = IOUtil.getResourceAsStream(TESTFILE_PATH, getClass().getClassLoader());
        assertNotNull(is);
        assertEquals("foo", IOUtil.readStream(is, "UTF-8"));
    }

}

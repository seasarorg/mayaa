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
package org.seasar.mayaa.impl.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.impl.standalone.FileGenerator.Argument;
import org.seasar.mayaa.impl.util.IOUtil;

/**
 * @author Koji Suga (Gluegent Inc.)
 */
public class FileGeneratorTest {

    /**
     * -v, --version
     *   バージョン番号を表示する
     *
     * @throws Throwable
     */
    @Test
    public void testParseVersion() throws Throwable {
        Argument result0 = FileGenerator.parse(array("-h"));
        assertFalse(result0.isVersion());

        Argument result1 = FileGenerator.parse(array("-v"));
        assertTrue(result1.isVersion());
        Argument result2 = FileGenerator.parse(array("--version"));
        assertTrue(result2.isVersion());
    }

    /**
     * -h, --help
     *   ヘルプを表示する
     *
     * @throws Throwable
     */
    @Test
    public void testParseHelp() throws Throwable {
        Argument result0 = FileGenerator.parse(array("-v"));
        assertFalse(result0.isHelp());

        Argument result1 = FileGenerator.parse(array("-h"));
        assertTrue(result1.isHelp());
        Argument result2 = FileGenerator.parse(array("--help"));
        assertTrue(result2.isHelp());
    }

    /**
     * -b, --base &lt;コンテキストルートのパス名&gt;
     *   basePath。必須。
     *
     * @throws Throwable
     */
    @Test
    public void testParseBase() throws Throwable {
        String expected = "C:\\home";

        Argument result1 = FileGenerator.parse(array("-b", expected));
        assertEquals(expected, result1.getBasePath());
        Argument result2 = FileGenerator.parse(array("--base", expected));
        assertEquals(expected, result2.getBasePath());
    }

    /**
     * -o, --output &lt;出力先フォルダ名&gt;
     *   outputPath。必須。
     *
     * @throws Throwable
     */
    @Test
    public void testParseOutput() throws Throwable {
        String expected = "C:\\home";

        Argument result1 = FileGenerator.parse(array("-o", expected));
        assertEquals(expected, result1.getOutputPath());
        Argument result2 = FileGenerator.parse(array("--output", expected));
        assertEquals(expected, result2.getOutputPath());
    }

    /**
     * -f, --filters &lt;ファイル名フィルタ&gt;
     *   ファイル名フィルタ。任意。
     *   デフォルトは".html"。
     *
     * @throws Throwable
     */
    @Test
    public void testParseFilters() throws Throwable {
        String expected = ".html;.*foobar.*";

        Argument result1 = FileGenerator.parse(array("-f", expected));
        assertEquals(expected, result1.getFilters());
        Argument result2 = FileGenerator.parse(array("--filters", expected));
        assertEquals(expected, result2.getFilters());
    }

    /**
     * -a, --attributes &lt;属性名と値のセット&gt;
     *   "name=value;name=value"の形。applicationスコープのattributeになる。
     *
     * @throws Throwable
     */
    @Test
    public void testParseAttributes() throws Throwable {
        String expected = "name1=value1;name2=value2";

        Argument result1 = FileGenerator.parse(array("-a", expected));
        assertEquals(expected, result1.getAttributes());
        Argument result2 = FileGenerator.parse(array("--attributes", expected));
        assertEquals(expected, result2.getAttributes());
    }

    /**
     * -p, --property &lt;ファイル名&gt;
     *   --attributesの代わりにプロパティファイルを使う。
     *
     * @throws Throwable
     */
    @Test
    public void testParseProperty() throws Throwable {
        String expected = "C:\\home\\attributes.properties";

        Argument result1 = FileGenerator.parse(array("-p", expected));
        assertEquals(expected, result1.getPropertyFilePath());
        Argument result2 = FileGenerator.parse(array("--property", expected));
        assertEquals(expected, result2.getPropertyFilePath());
    }

    /**
     * -v, --version
     *   バージョン番号を表示する
     * -h, --help
     *   ヘルプを表示する
     * -b, --base &lt;コンテキストルートのパス名&gt;
     *   basePath。必須。
     * -o, --output &lt;出力先フォルダ名&gt;
     *   outputPath。必須。
     * -f, --filters &lt;ファイル名フィルタ&gt;
     *   ファイル名フィルタ。任意。
     *   デフォルトは".html"。
     * -a, --attributes &lt;属性名と値のセット&gt;
     *   "name=value;name=value"の形。applicationスコープのattributeになる。
     * -p, --property &lt;ファイル名&gt;
     *   --attributesの代わりにプロパティファイルを使う。
     *
     * @throws Throwable
     */
    @Test
    public void testParseAll() throws Throwable {
        List<String> arguments = new ArrayList<>(12);
        arguments.add("-v");
        arguments.add("-h");
        arguments.add("-b");
        arguments.add("C:\\home\\base");
        arguments.add("-o");
        arguments.add("C:\\home\\output");
        arguments.add("-f");
        arguments.add(".html;.*foobar.*");
        arguments.add("-a");
        arguments.add("name1=value1;name2=value2");
        arguments.add("-p");
        arguments.add("C:\\home\\attributes.properties");

        Argument result1 = FileGenerator.parse(toStringArray(arguments));

        assertTrue(result1.isVersion());
        assertTrue(result1.isHelp());
        assertEquals(arguments.get(3), result1.getBasePath());
        assertEquals(arguments.get(5), result1.getOutputPath());
        assertEquals(arguments.get(7), result1.getFilters());
        assertEquals(arguments.get(9), result1.getAttributes());
        assertEquals(arguments.get(11), result1.getPropertyFilePath());
    }

    @Test
    public void testExtractAttributes() throws Throwable {
        List<String> arguments = new ArrayList<>(12);
        arguments.add("-b");
        arguments.add("C:\\home\\base");
        arguments.add("-o");
        arguments.add("C:\\home\\output");
        arguments.add("-f");
        arguments.add(".html;.*foobar.*");

        // 属性無し
        Argument result1 = FileGenerator.parse(toStringArray(arguments));
        assertNull(FileGenerator.extractAttributes(result1));

        // 属性を追加
        arguments.add("-a");
        arguments.add("name1=value1;name2 = value2");
        arguments.add("-p");
        arguments.add(getResourcePath("FileGeneratorTest_extractAttributes.properties"));

        Argument result2 = FileGenerator.parse(toStringArray(arguments));
        Map<Object, Object> attributes = FileGenerator.extractAttributes(result2);

        assertEquals("value1", attributes.get("name1"));
        assertEquals("value2", attributes.get("name2"));
        assertEquals("filevalue1", attributes.get("filename1"));
        assertEquals("filevalue2", attributes.get("filename2"));
    }

    public String[] array(String arg1) {
        return new String[]{ arg1 };
    }

    public String[] array(String arg1, String arg2) {
        return new String[]{ arg1, arg2 };
    }

    public String[] toStringArray(List<String> list) {
        return list.toArray(new String[list.size()]);
    }

    /**
     * 現在パッケージにあるファイルのフルパスを取得して返す。
     * ファイルが存在しない場合はIllegalArgumentExceptionを投げる。
     *
     * @param name ファイル名 (フォルダを含まない)
     * @return フルパスファイル名
     * @throws IllegalArgumentException ファイルが存在しない場合
     */
    public String getResourcePath(String name) {
        String folder = getClass().getPackage().getName().replace('.', '/');
        URL url = IOUtil.getResource(folder + '/' + name);
        if (url == null) {
            throw new IllegalArgumentException(folder + '/' + name + " is not found");
        }
        return new File(url.toExternalForm().substring("file:".length())).getPath();
    }

}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Undefined;
import org.seasar.mayaa.impl.cycle.scope.ParamScope;
import org.seasar.mayaa.impl.cycle.script.rhino.ScriptEnvironmentImpl;
import org.seasar.mayaa.impl.util.messagetest.MessageKey;
import org.seasar.mayaa.test.util.ManualProviderFactory;

/**
 * @author suga
 */
public class StringUtilTest {

    @Test
    public void testResolveEntity() {
        // 解決するエンティティのとき
        assertEquals("&", StringUtil.resolveEntity("&amp;"));
        assertEquals("<", StringUtil.resolveEntity("&lt;"));
        assertEquals(">", StringUtil.resolveEntity("&gt;"));
        assertEquals("\"", StringUtil.resolveEntity("&quot;"));

        // 架空のエンティティ名のとき
        assertEquals("&abcdefg;", StringUtil.resolveEntity("&abcdefg;"));

        // エンティティの閉じセミコロンがないとき
        assertEquals("&amp", StringUtil.resolveEntity("&amp"));

        // 複数のとき
        assertEquals("<\"&>", StringUtil.resolveEntity("&lt;&quot;&amp;&gt;"));

        // エンティティの前に字があるとき
        assertEquals("foo>", StringUtil.resolveEntity("foo&gt;"));

        // エンティティの後に字があるとき
        assertEquals("<foo", StringUtil.resolveEntity("&lt;foo"));

        // エンティティの間に字があるとき
        assertEquals("<foo>", StringUtil.resolveEntity("&lt;foo&gt;"));

        // エンティティの前後と間に字があるとき
        assertEquals("foo<bar>baz",
                StringUtil.resolveEntity("foo&lt;bar&gt;baz"));
    }

    @Test
    public void testPreparePath() {
        String path1 = "test.html";
        assertEquals("/test.html", StringUtil.preparePath(path1));

        String path2 = "/test.html";
        assertEquals("/test.html", StringUtil.preparePath(path2));

        String path3 = "test/";
        assertEquals("/test", StringUtil.preparePath(path3));

        String path4 = "/test/";
        assertEquals("/test", StringUtil.preparePath(path4));

        String path5 = "";
        assertEquals("", StringUtil.preparePath(path5));

        assertEquals("", StringUtil.preparePath(null));
    }

    @Test
    public void testToTrimedCharArray() {
        assertEquals("test", new String(StringUtil.toTrimedCharArray("test")));
        assertEquals("test", new String(StringUtil.toTrimedCharArray("  test")));
        assertEquals("test", new String(StringUtil.toTrimedCharArray("test  ")));
        assertEquals("test", new String(StringUtil.toTrimedCharArray("  test  ")));
        assertEquals("", new String(StringUtil.toTrimedCharArray("")));
    }

    @Test
    public void testParseURIQuery() {
        String separator = "$";

        String[] result1 = StringUtil.parseURIQuery("/foo/bar.html", separator);
        assertEquals("/foo/bar.html", result1[0]);
        assertEquals("", result1[1]);
        assertEquals("", result1[2]);

        String[] result2 = StringUtil.parseURIQuery("/foo/bar.html?name=value&amp;name2=value2", separator);
        assertEquals("/foo/bar.html", result2[0]);
        assertEquals("name=value&amp;name2=value2", result2[1]);
        assertEquals("", result2[2]);

        String[] result3 = StringUtil.parseURIQuery("/foo/bar.html?name=value&name2=value2#myfragment", separator);
        assertEquals("/foo/bar.html", result3[0]);
        assertEquals("name=value&name2=value2", result3[1]);
        assertEquals("myfragment", result3[2]);

        String[] result4 = StringUtil.parseURIQuery("/foo/bar.html#myfragment", separator);
        assertEquals("/foo/bar.html", result4[0]);
        assertEquals("", result4[1]);
        assertEquals("myfragment", result4[2]);

        String[] result5 = StringUtil.parseURIQuery("/foo/bar$hoge.html#myfragment", separator);
        assertEquals("/foo/bar.html", result5[0]);
        assertEquals("", result5[1]);
        assertEquals("myfragment", result5[2]);

        // Apacheの場合の$エスケープ対応
        String[] result6 = StringUtil.parseURIQuery("/foo/bar\\$hoge.html?name=value#myfragment", separator);
        assertEquals("/foo/bar.html", result6[0]);
        assertEquals("name=value", result6[1]);
        assertEquals("myfragment", result6[2]);
    }

    @Test
    public void testParsePath() {
        String separator = "$";
        String path1 = "/test_component.html";
        String path2 = "/test_component$ja.html";

        String[] result1 = StringUtil.parsePath(path1, separator);
        assertEquals("/test_component", result1[0]);
        assertEquals("", result1[1]);
        assertEquals("html", result1[2]);

        String[] result2 = StringUtil.parsePath(path2, separator);
        assertEquals("/test_component", result2[0]);
        assertEquals("ja", result2[1]);
        assertEquals("html", result2[2]);
    }

    @Test
    public void testParsePath2() {
        String separator = ".";
        String path1 = "/test_component.html";
        String path2 = "/test_component.ja.html";

        String[] result1 = StringUtil.parsePath(path1, separator);
        assertEquals("/test_component", result1[0]);
        assertEquals("", result1[1]);
        assertEquals("html", result1[2]);

        String[] result2 = StringUtil.parsePath(path2, separator);
        assertEquals("/test_component", result2[0]);
        assertEquals("ja", result2[1]);
        assertEquals("html", result2[2]);
    }

    @Test
    public void testParsePath_notStartsWithSlash() {
        String separator = ".";
        String path1 = "test_component.html";
        String path2 = "test_component.ja.html";

        String[] result1 = StringUtil.parsePath(path1, separator);
        assertEquals("test_component", result1[0]);
        assertEquals("", result1[1]);
        assertEquals("html", result1[2]);

        String[] result2 = StringUtil.parsePath(path2, separator);
        assertEquals("test_component", result2[0]);
        assertEquals("ja", result2[1]);
        assertEquals("html", result2[2]);
    }

    @Test
    public void testIsRelativePath() {
        assertTrue(StringUtil.isRelativePath("./test.html"));
        assertTrue(StringUtil.isRelativePath("./../test.html"));
        assertFalse(StringUtil.isRelativePath(""));
        assertFalse(StringUtil.isRelativePath("../test.html"));
        assertFalse(StringUtil.isRelativePath("test.html"));
        assertFalse(StringUtil.isRelativePath("/../test.html"));
    }

    @Test
    public void testAdjustRelativePath() {
        String base = "/page/test_page";

        String path1 = "/test_component";
        String path2 = "test_component";
        String path3 = "./test_component";
        String path4 = "../test_component";
        String path5 = "./../test_component";
        String path6 = "./../../test_component";
        String path7 = "";
        String path8 = "./";
        String path9 = "../";
        String path10 = "./../";
        // 間に /../ や /./ が入るパターンは考慮しない

        assertEquals(path1,
                StringUtil.adjustRelativePath(base, path1));
        assertEquals(path2,
                StringUtil.adjustRelativePath(base, path2));
        assertEquals("/page/test_component",
                StringUtil.adjustRelativePath(base, path3));
        assertEquals(path4,
                StringUtil.adjustRelativePath(base, path4));
        assertEquals("/test_component",
                StringUtil.adjustRelativePath(base, path5));
        assertEquals("/test_component",
                StringUtil.adjustRelativePath(base, path6));
        assertEquals(path7,
                StringUtil.adjustRelativePath(base, path7));
        assertEquals("/page/",
                StringUtil.adjustRelativePath(base, path8));
        assertEquals(path9,
                StringUtil.adjustRelativePath(base, path9));
        assertEquals("/",
                StringUtil.adjustRelativePath(base, path10));

        try {
            StringUtil.adjustRelativePath(base, "");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        try {
            StringUtil.adjustRelativePath("", path1);
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testAdjustRelativePathForComponent() {
        String hostPage = "/main/test_page.html";

        String path1 = "/test_component.html";
        String path2 = "test_component.html";
        String path3 = "/component/page/test_component.html";
        String path4 = "../test_component.html";
        String path5 = "/component/test_component.html";
        String path6 = "/test_component.html";
        String path7 = "";
        String path8 = "/component/page/";
        String path9 = "../";
        String path10 = "/component/";
        String path11 = "/main/foo.html";
        // 間に /../ や /./ が入るパターンは考慮しない

        assertEquals("../test_component.html",
                StringUtil.adjustContextRelativePath(hostPage, path1));
        assertEquals(path2,
                StringUtil.adjustContextRelativePath(hostPage, path2));
        assertEquals("../component/page/test_component.html",
                StringUtil.adjustContextRelativePath(hostPage, path3));
        assertEquals(path4,
                StringUtil.adjustContextRelativePath(hostPage, path4));
        assertEquals("../component/test_component.html",
                StringUtil.adjustContextRelativePath(hostPage, path5));
        assertEquals("../test_component.html",
                StringUtil.adjustContextRelativePath(hostPage, path6));
        assertEquals(path7,
                StringUtil.adjustContextRelativePath(hostPage, path7));
        assertEquals("../component/page/",
                StringUtil.adjustContextRelativePath(hostPage, path8));
        assertEquals(path9,
                StringUtil.adjustContextRelativePath(hostPage, path9));
        assertEquals("../component/",
                StringUtil.adjustContextRelativePath(hostPage, path10));
        assertEquals("foo.html",
                StringUtil.adjustContextRelativePath(hostPage, path11));

        try {
            StringUtil.adjustRelativePath(hostPage, "");
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
        try {
            StringUtil.adjustRelativePath("", path1);
        } catch (IllegalArgumentException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testReplaceSystemProperties() {
        String staticValue = "org.seasar.mayaa.MayaaServlet.class";
        String dynamicValue1 = "${ java.home }";
        String dynamicValue2 = "${ java.home }/bin";
        String dynamicValue3 = "file://${ java.home }";
        String dynamicValue4 = "${ java.home };${java.home};$ {java.home}";
        String nonDynamicValue1 = "${ java.home";
        String notExsists = "${ java.home.nothing }";

        String javaHome = System.getProperty("java.home");

        assertEquals(staticValue,
                StringUtil.replaceSystemProperties(staticValue));
        assertEquals(javaHome,
                StringUtil.replaceSystemProperties(dynamicValue1));
        assertEquals(javaHome + "/bin",
                StringUtil.replaceSystemProperties(dynamicValue2));
        assertEquals("file://" + javaHome,
                StringUtil.replaceSystemProperties(dynamicValue3));
        assertEquals(javaHome + ";" + javaHome + ";$ {java.home}",
                StringUtil.replaceSystemProperties(dynamicValue4));

        assertEquals(nonDynamicValue1,
                StringUtil.replaceSystemProperties(nonDynamicValue1));
        try {
            StringUtil.replaceSystemProperties(notExsists);
            fail();
        } catch (IllegalStateException expected) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetMessage() {
        String message = StringUtil.getMessage(MessageKey.class, 0, "");
        assertEquals("test message", message);
        String message1 = StringUtil.getMessage(MessageKey.class, 1, "");
        assertEquals("test message 1", message1);
        String message2 = StringUtil.getMessage(
                MessageKey.class, 2, new String[] { "test" });
        assertEquals("test message 2", message2);
    }

    @Test
    public void testNoPropertiesFile() {
        String message = StringUtil.getMessage(Object.class, 0, "");
        assertEquals("!java.lang.Object!", message);
    }

    @Test
    public void testEscapeXml() {
        assertEquals("", StringUtil.escapeXml(null));
        assertEquals("&amp;amp;", StringUtil.escapeXml("&amp;"));
        assertEquals("test&lt;&gt;test", StringUtil.escapeXml("test<>test"));
        assertEquals("&quot;&amp;gt;&quot;", StringUtil.escapeXml("\"&gt;\""));
        // "'" はエスケープしないようにした (rev.2811)
        assertEquals("'&amp;gt;'", StringUtil.escapeXml("'&gt;'"));
        assertEquals("test", StringUtil.escapeXml("test"));
    }

    @Test
    public void testEscapeXmlWithoutAmp() {
        assertEquals("", StringUtil.escapeXmlWithoutAmp(null));
        assertEquals("&amp;", StringUtil.escapeXmlWithoutAmp("&amp;"));
        assertEquals("test&lt;&gt;test", StringUtil.escapeXmlWithoutAmp("test<>test"));
        assertEquals("&quot;&gt;&quot;", StringUtil.escapeXmlWithoutAmp("\"&gt;\""));
        // "'" はエスケープしないようにした (rev.2811)
        assertEquals("'&gt;'", StringUtil.escapeXmlWithoutAmp("'&gt;'"));
        assertEquals("test", StringUtil.escapeXmlWithoutAmp("test"));
    }

    @Test
    public void testEscapeEol() {
        assertEquals("", StringUtil.escapeEol(null, false));
        assertEquals("test", StringUtil.escapeEol("test", false));
        assertEquals("<br />", StringUtil.escapeEol("\r\n", false));
        assertEquals("<br /><br />", StringUtil.escapeEol("\n\n", false));
        assertEquals("<br /><br />", StringUtil.escapeEol("\r\r", false));
        assertEquals("<br /><br />", StringUtil.escapeEol("\r\n\r", false));
        assertEquals("<br /><br />", StringUtil.escapeEol("\r\r\n", false));
    }

    @Test
    public void testEscapeEol_Html() {
        assertEquals("", StringUtil.escapeEol(null, true));
        assertEquals("test", StringUtil.escapeEol("test", true));
        assertEquals("<br>", StringUtil.escapeEol("\r\n", true));
        assertEquals("<br><br>", StringUtil.escapeEol("\n\n", true));
        assertEquals("<br><br>", StringUtil.escapeEol("\r\r", true));
        assertEquals("<br><br>", StringUtil.escapeEol("\r\n\r", true));
        assertEquals("<br><br>", StringUtil.escapeEol("\r\r\n", true));
    }

    @Test
    public void testEscapeWhitespace() {
        assertEquals("", StringUtil.escapeWhitespace(null));
        assertEquals(
                "&#xd;&#xa; &#x9;", StringUtil.escapeWhitespace("\r\n \t"));
        assertEquals("test", StringUtil.escapeWhitespace("test"));
    }

    @Test
    public void testTimes() {
        assertEquals("", StringUtil.times("abc", 0));
        assertEquals("abc", StringUtil.times("abc", 1));
        assertEquals("", StringUtil.times("abc", -1));
        assertEquals("abcabcabcabcabc", StringUtil.times("abc", 5));
    }

    @Test
    public void testArraycopy() {
        String[] src0 = new String[0];
        String[] copy0 = StringUtil.arraycopy(src0);
        assertNotSame(src0, copy0);
        for (int i = 0; i < src0.length; i++) {
            assertEquals(src0[i], copy0[i], "0:" + i);
        }

        String[] src1 = new String[] { "1" };
        String[] copy1 = StringUtil.arraycopy(src1);
        assertNotSame(src1, copy1);
        for (int i = 0; i < src1.length; i++) {
            assertEquals(src1[i], copy1[i], "0:" + i);
        }

        String[] src3 = new String[] { "1", "2", "3" };
        String[] copy3 = StringUtil.arraycopy(src3);
        assertNotSame(src3, copy3);
        for (int i = 0; i < src3.length; i++) {
            assertEquals(src3[i], copy3[i], "0:" + i);
        }
    }

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtil.isEmpty((String) null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty("test"));
        assertFalse(StringUtil.isEmpty(" "));
    }

    @Test
    public void testIsEmpty_Object() {
        ManualProviderFactory.setUp(this);// ScriptUtil#isEmptyのために準備
        ScriptEnvironmentImpl scriptEnvironment = ManualProviderFactory.SCRIPT_ENVIRONMENT;
        scriptEnvironment.setBlockSign("$");
        scriptEnvironment.addAttributeScope(new ParamScope());
        scriptEnvironment.initScope();
        scriptEnvironment.startScope(null);

        assertTrue(StringUtil.isEmpty((Object) null));
        assertTrue(StringUtil.isEmpty((Object) ""));
        assertTrue(StringUtil.isEmpty(Undefined.instance));
        assertFalse(StringUtil.isEmpty((Object) "test"));
        assertFalse(StringUtil.isEmpty(new Object()));
    }

}

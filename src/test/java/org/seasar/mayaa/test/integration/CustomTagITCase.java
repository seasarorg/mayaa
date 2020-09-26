package org.seasar.mayaa.test.integration;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CustomTagITCase extends WebDriverBase {
    //@formatter:off
    final static Object[][] data = new Object[][] {
        /* TestCustomTag */
        {
            "scope",
            "/tests/customtag/scopetest.html",
            new Command[] {
                verifyTitle("tests_4_01"),
                verifyTextNotPresent("dummy"),
                verifyTextPresent("nothing atbegin 0"),
                verifyTextPresent("nothing nested 0"),
                verifyTextPresent("nothing atend 0"),
                verifyTextNotPresent("nothing atbegin 1"),
                verifyTextNotPresent("nothing nested 1"),
                verifyTextPresent("nothing atend 1"),
                verifyTextNotPresent("nothing atbegin 2"),
                verifyTextPresent("nothing nested 2"),
                verifyTextNotPresent("nothing atend 2"),
                verifyTextPresent("at_begin"),
                verifyTextPresent("nested"),
                verifyTextPresent("at_end")
            }
        }, {
            "buffered",
            "/tests/customtag/eval_body_buffered.html",
            new Command[] {
                verifyTitle("tests_4_02"),
                verifyTextNotPresent("original-body"),
                verifyTextPresent("replaced-body")
            }
        }, {
            "simpletag",
            "/tests/customtag/simpletest.html",
            new Command[] {
                verifyTitle("tests_4_03"),
                verifyTextNotPresent("dummy"),
                verifyText("//span[@id=\"hello1\"]", "hello TestTagc&lt;l&amp;a\"s&gt;s"), // innerHTMLとして取得すると &quot; は実体参照とならない
                verifyText("//span[@id=\"hello2\"]", "[c&amp;lt;l&amp;amp;a&amp;quot;s&amp;gt;s]"), // FIXME:class属性をデフォルトスコープから取得すると実体参照を解釈しないようだが、期待している挙動か？
                verifyText("//span[@id=\"hello3\"]", "[c&amp;lt;l&amp;amp;a&amp;quot;s&amp;gt;s]"),
                verifyText("//span[@id=\"hello4\"]", "[c&amp;lt;l&amp;amp;a&amp;quot;s&amp;gt;s]"),
                verifyText("//span[@id=\"hello5\"]",
                "hello TestTag parent:org.apache.taglibs.standard.tag.rt.core.IfTag")
            }
        }, {
            "dynamic-attribute",
            "/tests/customtag/dynamic_attribute.html",
            new Command[] {
                verifyTitle("tests_4_04"),
                verifyText("//span[@id=\"message1\"]", "nothing"),
                verifyText("//span[@id=\"message2\"]", "dynamic DYNAMIC"),
                verifyText("//span[@id=\"message3\"]", "dynamic 30"),
                verifyText("//span[@id=\"message4\"]", "not support"),
                verifyText("//span[@id=\"message5\"]", "not support"),
                verifyText("//span[@id=\"message6\"]", "not support")
            }
        }, {
            "dynamic-attribute",
            "/tests/customtag/dynamic_attribute_wrong.html",
            new Command[] {
                verifyTextPresent("DynamicAttributeNotSupportTag"),
                verifyTextPresent("javax.servlet.jsp.tagext.DynamicAttributes")
            }
        }, {
            "rtexprvalue attribute",
            "/tests/customtag/rtexprtest.html",
            new Command[] {
                verifyTitle("tests_4_05"),
                verifyText("//span[@id=\"expr1\"]", "30"),
                verifyText("//span[@id=\"expr2\"]", "STRING"),
                verifyText("//span[@id=\"expr3\"]", "${20 - 10}"),
                verifyText("//span[@id=\"expr4\"]", "STRING")
            }
        }, {
            "replace_injection_attribute",
            "/tests/customtag/replace_injection_attribute.html",
            new Command[] {
                verifyTitle("tests_4_06"),
                verifyText("//p[1]", "good"),
                verifyText("//p[2]", "good")
            }
        }, {
            "simpletag-dynamic-attribute",
            "/tests/customtag/simple_dynamic_test.html",
            new Command[] {
                verifyTitle("tests_4_07"),
                verifyText("//span[@id=\"message1\"]", "dynamic=DYNAMIC")
            }
        }, {
            "TagExtraInfo#getVariableInfo returns null",
            "/tests/customtag/vinulltest.html",
            new Command[] {
                verifyTitle("tests_4_08"),
                verifyText("//span[@id=\"tag\"]", "hello TestTag")
            }
        }, {
            "simpletag body",
            "/tests/customtag/simplebodytest.html",
            new Command[] {
                verifyTitle("tests_4_09"),
                verifyText("//span[@id=\"test1\"]", "hello TestTag body:body1"),
                verifyText("//span[@id=\"hello2\"]", "hello TestTag body:body2"),
                verifyText("//span[@id=\"test3\"]", "hello TestTag body:body3[child write]"),
                verifyText("//span[@id=\"test4\"]", "hello TestTagComponent1 body:body4[doBase write]"),
                verifyText("//span[@id=\"test5\"]",
                "hello TestTag body:hello TestTagComponent2 body:body5[doBase write 2]")
            }
        }, {
            "simpletag body",
            "/tests/customtag/emptybody.html",
            new Command[] {
                verifyTitle("tests_4_10"),
                verifyText("//span[@id=\"test1\"]", "bodycontentempty")
            }
        },
    };
    //@formatter:on

    @Parameters(name = "{0}: path={1}")
    public static Iterable<Object[]> data() throws Throwable {
        return Arrays.asList(data);
    }

    public CustomTagITCase(String title, String path, Command[] commands) {
        super(title, path, commands);
    }

    @BeforeClass
    public static void setUpClass() {
        setUpSelenide();
    }

    @Test
    public void test() {
        runTest();
    }
}

package org.seasar.mayaa.test.integration;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class CustomTagITCase extends WebDriverBase {
    //@formatter:off
    final static Stream<Arguments> data = Stream.of(
        arguments(
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
        ),
        arguments(
            "buffered",
            "/tests/customtag/eval_body_buffered.html",
            new Command[] {
                verifyTitle("tests_4_02"),
                verifyTextNotPresent("original-body"),
                verifyTextPresent("replaced-body")
            }
        ),
        arguments(
            "simpletag",
            "/tests/customtag/simpletest.html",
            new Command[] {
                verifyTitle("tests_4_03"),
                verifyTextNotPresent("dummy"),
                verifyText("//span[@id=\"hello1\"]", "hello TestTagc&lt;l&amp;a\"s&gt;s"), // innerHTMLとして取得すると &quot; は実体参照とならない
                verifyText("//span[@id=\"hello2\"]", "[c&lt;l&amp;a\"s&gt;s]"),
                verifyText("//span[@id=\"hello3\"]", "[c&lt;l&amp;a\"s&gt;s]"),
                verifyText("//span[@id=\"hello4\"]", "[c&lt;l&amp;a\"s&gt;s]"),
                verifyText("//span[@id=\"hello5\"]",
                "hello TestTag parent:org.apache.taglibs.standard.tag.rt.core.IfTag")
            }
        ),
        arguments(
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
        ),
        arguments(
            "dynamic-attribute",
            "/tests/customtag/dynamic_attribute_wrong.html",
            new Command[] {
                verifyTextPresent("DynamicAttributeNotSupportTag"),
                verifyTextPresent("jakarta.servlet.jsp.tagext.DynamicAttributes")
            }
        ),
        arguments(
            "rtexprvalue attribute",
            "/tests/customtag/rtexprtest.html",
            new Command[] {
                verifyTitle("tests_4_05"),
                verifyText("//span[@id=\"expr1\"]", "30"),
                verifyText("//span[@id=\"expr2\"]", "STRING"),
                verifyText("//span[@id=\"expr3\"]", "${20 - 10}"),
                verifyText("//span[@id=\"expr4\"]", "STRING")
            }
        ),
        arguments(
            "replace_injection_attribute",
            "/tests/customtag/replace_injection_attribute.html",
            new Command[] {
                verifyTitle("tests_4_06"),
                verifyText("//p[1]", "good"),
                verifyText("//p[2]", "good")
            }
        ),
        arguments(
            "simpletag-dynamic-attribute",
            "/tests/customtag/simple_dynamic_test.html",
            new Command[] {
                verifyTitle("tests_4_07"),
                verifyText("//span[@id=\"message1\"]", "dynamic=DYNAMIC")
            }
        ),
        arguments(
            "TagExtraInfo#getVariableInfo returns null",
            "/tests/customtag/vinulltest.html",
            new Command[] {
                verifyTitle("tests_4_08"),
                verifyText("//span[@id=\"tag\"]", "hello TestTag")
            }
        ),
        arguments(
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
        ),
        arguments(
            "simpletag body",
            "/tests/customtag/emptybody.html",
            new Command[] {
                verifyTitle("tests_4_10"),
                verifyText("//span[@id=\"test1\"]", "bodycontentempty")
            }
        )
    );
    //@formatter:on

    @BeforeAll
    public static void setUpClass() {
        setUpSelenide();
    }

    @ParameterizedTest(name = "{0}: path={1}")
    @MethodSource("dataProvider")
    @EnabledIfSystemProperty(named = "inMaven", matches = "1")
    public void test(String title, String path, Command[] commands) {
        runTest(title, path, commands);
    }

    static Stream<Arguments> dataProvider() {
        return data;
    }
}

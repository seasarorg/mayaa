package org.seasar.mayaa.test.integration;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RenderITCase extends WebDriverBase {
    //@formatter:off
    final static Stream<Arguments> data = Stream.of(
        arguments(
            "no-injection",
            "/tests/engine/inject_no.html",
            new Command[] {
                verifyTitle("tests_1_01"),
                verifyText("//body/h1", "render test"),
                verifyText("//body/div[@class=\"main\"]/h2", "no inject"),
                verifyText("//div[@class=\"box\"]", "Plain HTML"),
                verifyElementPresent("//meta"),
                verifyElementPresent("//link")
            }
        ),
        arguments(
            "injection with .mayaa",
            "/tests/engine/inject_mayaa.html",
            new Command[] {
                verifyText("//body/h1", "render test"),
                verifyText("//body/div[@class=\"main\"]/h2", "mayaa inject"),
                verifyText("//span[@class=\"message1\"]", "hello"),
                verifyElementNotPresent("//span[@id=\"message1\"]"),
                verifyText("//span[@id=\"message2\"]", "dummy")
            }
        ),
        arguments(
            "no-injection",
            "/tests/engine/inject_no.html",
            new Command[] {
                verifyTitle("tests_1_01"),
                verifyText("//body/h1", "render test"),
                verifyText("//body/div[@class=\"main\"]/h2", "no inject"),
                verifyText("//div[@class=\"box\"]", "Plain HTML"),
                verifyElementPresent("//meta"),
                verifyElementPresent("//link")
            }
        ),
        arguments(
            "injection with .mayaa",
            "/tests/engine/inject_mayaa.html",
            new Command[] {
                verifyTitle("tests_1_02"),
                verifyText("//body/h1", "render test"),
                verifyText("//body/div[@class=\"main\"]/h2", "mayaa inject"),
                verifyText("//span[@class=\"message1\"]", "hello"),
                verifyElementNotPresent("//span[@id=\"message1\"]"),
                verifyText("//span[@id=\"message2\"]", "dummy")
            }
        ),
        arguments(
            "injection on template",
            "/tests/engine/inject_template.html",
            new Command[] {
                verifyTitle("tests_1_03"),
                verifyText("//body/h1", "render test"),
                verifyText("//body/div[@class=\"main\"]/h2", "template inject"),
                verifyText("//div[@class=\"box\"]", "hello"),
                verifyTextNotPresent("dummy"),
                verifyElementNotPresent("//div/span")
            }
        ),
        arguments(
            "replace",
            "/tests/engine/replace.html",
            new Command[] {
                verifyTitle("tests_1_04"),
                verifyText("//body/span[@id=\"message1\"]", "hello1"),
                verifyText("//body/span[@id=\"message2\"]", "hello2"),
                verifyText("//body/span[@id=\"error1\"]", "no attributable processor."),
                verifyTextNotPresent("dummy1"),
                verifyTextNotPresent("dummy2"),
                verifyTextNotPresent("dummy3"),
                verifyElementPresent("//span[@id=\"message1\"]"),
                verifyElementPresent("//span[@id=\"message2\"]"),
                verifyElementPresent("//span[@id=\"error1\"]"),
                verifyElementNotPresent("//span[@id=\"message2_body\"]"),
                verifyElementNotPresent("//span[@id=\"message3\"]")
            }
        ),
        arguments(
            "ignore",
            "/tests/engine/ignore_mayaa.html",
            new Command[] {
                verifyTitle("tests_1_05"),
                verifyText("//body/span[@id=\"message1\"]", "dummy1"),
                verifyTextNotPresent("hello1"),
                verifyTextNotPresent("hello2"),
                verifyTextPresent("dummy2"),
                verifyElementPresent("//span[@id=\"message1\"]"),
                verifyElementPresent("//span[@id=\"message2\"]")
            }
        ),
        arguments(
            "ignore",
            "/tests/engine/inject_xpath.html",
            new Command[] {
                verifyTitle("tests_1_06"),
                // verifyText("//body", "hello first good deep no array"),
                verifyTextPresent("hello"),
                verifyText("//span[@class=\"message1\"]", "hello"),
                verifyText("//span[@class=\"message2\"]", "dummy")
            }
        ),
        arguments(
            "xmlns # namespaceを調べられるなら追加すること：現状不明",
            "/tests/engine/xout.html",
            new Command[] {
                verifyTitle("tests_1_07"),
                verifyText("//p[@id=\"message\"]", "hello"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "escape",
            "/tests/engine/escape.html",
            new Command[] {
                verifyTitle("tests_1_08"),
                verifyText("//span[@id=\"message1\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message2\"]", "true"),
                verifyText("//span[@id=\"message3\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message4\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message5\"]", "true"),
                verifyText("//span[@id=\"message6\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message7\"]/b[@id=\"message7_2\"]", "&lt;&amp;amp;&gt;"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "escape",
            "/tests/engine/escape.xhtml",
            new Command[] {
                verifyTitle("tests_1_09"),
                verifyText("//span[@id=\"message1\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message2\"]", "true"),
                verifyText("//span[@id=\"message3\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message4\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message5\"]", "true"),
                verifyText("//span[@id=\"message6\"]", "&lt;&amp;amp;&gt;"),
                verifyText("//span[@id=\"message7\"]/b[@id=\"message7_2\"]", "&lt;&amp;amp;&gt;"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
        //     "escape 目視", // htmlタグに xhtmlのxmlns定義を明示することで実行可能になるが、、
        //     "/tests/engine/escape.xml",
        //     new Command[] {
        //         verifyTitle("tests_1_10"),
        //         verifyText("//span[@id=\"message1\"]", "&lt;&amp;amp;&gt;"),
        //         verifyText("//span[@id=\"message2\"]", "true"),
        //         verifyText("//span[@id=\"message3\"]", "&lt;&amp;amp;&gt;"),
        //         verifyText("//span[@id=\"message4\"]", "&lt;&amp;amp;&gt;"),
        //         verifyText("//span[@id=\"message5\"]", "true"),
        //         verifyText("//span[@id=\"message6\"]", "&lt;&amp;amp;&gt;"),
        //         verifyText("//span[@id=\"message7\"]/b[@id=\"message7_2\"]", "&lt;&amp;amp;&gt;"),
        //         verifyTextNotPresent("dummy")
        //     }
        // }, {
            "beforeRender",
            "/tests/engine/beforeRender.html",
            new Command[] {
                verifyTitle("tests_1_11"),
                verifyTextPresent("hello1"),
                verifyTextPresent("hello2"),
                verifyTextNotPresent("dummy"),
                verifyTextNotPresent("hello11"),
                verifyTextNotPresent("hello22")
            }
        ),
        arguments(
            "template_attribute",
            "/tests/engine/template_attribute.html",
            new Command[] {
                verifyTitle("tests_1_12"),
                verifyText("//span[@id=\"message1\"]", "&lt;b&gt;hello&lt;/b&gt;"),
                verifyText("//span[@id=\"message2\"]/b", "hello"),
                verifyText("//span[@id=\"message3\"]", "on Template3"),
                verifyText("//span[@id=\"message4\"]", "on Mayaa4"),
                verifyText("//span[@id=\"message5\"]", "001.1"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "script",
            "/tests/engine/script.html",
            new Command[] {
                verifyTitle("tests_1_13"),
                verifyText("//span[@class=\"script1\"]", "script1"),
                verifyAttribute("//p[@class=\"script2\"]", "align", "right"),
                verifyText("//span[@class=\"script3\"]", "exact:http://mayaa.seasar.org"),
                verifyText("//span[@class=\"script4\"]", "script4Value"),
                verifyText("//span[@class=\"script5\"]", ""),
                verifyText("//span[@class=\"script6\"]", "6"),
                verifyText("//span[@class=\"script7\"]", "7"),
                verifyText("//span[@class=\"script8\"]", "8"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey0\"]", "10"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue0\"]", "foo_b"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey1\"]", "11"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue1\"]", "foo_s"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey2\"]", "12"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue2\"]", "foo_i"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey3\"]", "13"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue3\"]", "foo_l"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey4\"]", "10.1"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue4\"]", "foo_f"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey5\"]", "10.2"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue5\"]", "foo_d"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey6\"]", "20"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue6\"]", "bar_i"),
                verifyText("//dl[@id=\"numberList1\"]/dt[@id=\"numberList1numberKey7\"]", "20.1"),
                verifyText("//dl[@id=\"numberList1\"]/dd[@id=\"numberList1numberValue7\"]", "bar_d"),
                /* 10.2のみFloatなので、Rhinoのnumberと互換性があり拾える。 */
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey0\"]", "10"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue0\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey1\"]", "11"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue1\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey2\"]", "12"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue2\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey3\"]", "13"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue3\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey4\"]", "10.1"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue4\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey5\"]", "10.2"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue5\"]", "foo_d"),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey6\"]", "20"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue6\"]", ""),
                verifyText("//dl[@id=\"numberList2\"]/dt[@id=\"numberList2numberKey7\"]", "20.1"),
                verifyText("//dl[@id=\"numberList2\"]/dd[@id=\"numberList2numberValue7\"]", "")
            }
        ),
        arguments(
            "prefix no xmlns",
            "/tests/engine/no_xmlns.html",
            new Command[] {
                verifyTitle("tests_1_14"),
                verifyTextPresent("prefix")
            }
        ),
        arguments(
            "forward",
            "/tests/engine/forward/forward.html",
            new Command[] {
                verifyTitle("tests_1_15"),
                verifyTextPresent("forwarded")
            }
        ),
        arguments(
            "redirect",
            "/tests/engine/forward/redirect.html",
            new Command[] {
                verifyTitle("tests_1_16"),
                verifyTextPresent("redirected")
            }
        ),
        arguments(
            "handle error and forward",
            "/tests/engine/forward/error_forward.html",
            new Command[] {
                verifyTitle("tests_1_17"),
                verifyTextPresent("error_forwarded")
            }
        ),
        arguments(
            "handle error and redirect",
            "/tests/engine/forward/error_redirect.html",
            new Command[] {
                verifyTitle("tests_1_18"),
                verifyTextPresent("error_redirected")
            }
        ),
        arguments(
            "required test - null",
            "/tests/engine/required.html",
            new Command[] {
                verifyTextPresent(
                "NoRequiredPropertyException"),
            }
        ),
        arguments(
                "required test - empty",
            "/tests/engine/required_not_empty.html",
            new Command[] {
                verifyTextPresent(
                "NoRequiredPropertyException")
            }
        ),
        arguments(
            "undefined identifier",
            "/tests/engine/undefined_identifier.html",
            new Command[] {
                verifyTitle("tests_1_21"),
                verifyText("//form/div[1]", "一つ選択"),
                verifyText("//form/div[2]", "複数選択")
            }
        ),
        arguments(
            "cdata",
            "/tests/engine/cdata.html",
            new Command[] {
                verifyTitle("tests_1_22"),
                verifyText("message1", "&lt;hello1&gt;"),
                verifyText("message2", "&lt;hello2&gt;")
            }
        ),
        arguments(
                "forward under WEB-INF",
            "/tests/engine/forward/forward_to_webinf.html",
            new Command[] {
                verifyTitle("tests_1_23"),
                verifyTextPresent("forwarded")
            }
        ),
        arguments(
            "charset変換で文字化けしないことを確認_HTML",
            "/tests/engine/charset_html.html",
            new Command[] {
                verifyTitle("tests_1_24"),
                verifyText("bake", "～∥－―￢￡Φ①"),
                verifyAttribute("//meta", "content", "text/html; charset=Shift_JIS")
            }
        ),
        arguments(
            "charset変換で文字化けしないことを確認_XHTML",
            "/tests/engine/charset_xhtml.html",
            new Command[] {
                verifyTitle("tests_1_25"),
                verifyText("bake", "～∥－―￢￡Φ①"),
                verifyAttribute("//meta", "content", "text/html; charset=Shift_JIS")
            }
        ),
        arguments(
            "SSI include 記述を m:insert へ変換",
            "/tests/engine/inject_include.html",
            new Command[] {
                verifyTitle("tests_1_26"),
                verifyText("component", "component2"),
                verifyText("li1", "111"),
                verifyText("li2", "222")
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

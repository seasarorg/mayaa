package org.seasar.mayaa.test.integration;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ComponentITCase extends WebDriverBase {
    //@formatter:off
    /* TestComponent */
    final static Stream<Arguments> data = Stream.of(
        arguments(
            "simple",
            "/tests/component/component1_client.html",
            new Command[] {
                verifyTitle("tests_2_01"),
                verifyElementPresent("//div[@id=\"comp\"]"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "component value"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
                "direct component",
            "/tests/component/component1.html",
            new Command[] {
                verifyTitle("tests_2_02"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "component value")
            }
        ),
        arguments(
            "informal parameter",
            "/tests/component/component2_client.html",
            new Command[] {
                verifyTitle("tests_2_03"),
                verifyElementPresent("//div[@id=\"comp\"]"),
                verifyElementNotPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"comp\"]/div"),
                verifyText("//div[@id=\"comp\"]/div", "256"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "relative",
            "/tests/component/relative/component3_client.html",
            new Command[] {
                verifyTitle("tests_2_04"),
                verifyElementPresent("//div[@id=\"comp\"]"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "component value"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "recursive",
            "/tests/component/recursive.html",
            new Command[] {
                verifyTitle("tests_2_05"),
                verifyText(
                "//div[@class=\"main\"]/div[@class=\"inner\"]/div[@class=\"inner\"]/div[@id=\"innerBox\"]/"
                 + "div[@class=\"inner\"]/div[@id=\"innerBox\"]/div[@class=\"inner\"]/div[@id=\"innerBox\"]/"
                 + "div[@class=\"inner\"]/div[@id=\"innerBox\"]/div[@class=\"inner\"]/div[@id=\"innerBox\"]/"
                 + "div[@class=\"inner\"]/div[@id=\"innerBox\"]/div[@class=\"inner\"]/div[@id=\"innerBox\"]/"
                 + "div[@class=\"inner\"]/div[@id=\"innerBox\"]/div[@class=\"inner\"]/div[@id=\"innerBox\"]/"
                 + "div[@class=\"inner\"]/div[@id=\"innerBox\"]",
                "end"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "binding",
            "/tests/component/binding.html",
            new Command[] {
                verifyTitle("tests_2_06"),
                verifyText("//div[@class=\"main\"]", "hello"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "binding recursive",
            "/tests/component/binding_recursive.html",
            new Command[] {
                verifyTitle("tests_2_07"),
                verifyText(
                "//div[@class=\"main\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]/"
                + "div[@class=\"inner2\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]/"
                + "div[@class=\"inner2\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]/div[@class=\"inner2\"]",
                "end"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "runtime path change",
            "/tests/component/component123_client.html",
            new Command[] {
                verifyTitle("tests_2_08"),
                verifyText("//div/div[1]/div", "component value"),
                verifyText("//div[2]", "256"),
                verifyText("//div[3]/div", "component value")
            }
        ),
        arguments(
            "find parent",
            "/tests/component/component3_client.html",
            new Command[] {
                verifyTitle("tests_2_09"),
                verifyElementPresent("//div[@id=\"comp\"]"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "component value"),
                verifyText("//span[@id=\"simple\"]",
                "hello TestTag parent:org.apache.taglibs.standard.tag.rt.core.IfTag"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "no parent",
            "/tests/component/component3.html",
            new Command[] {
                verifyTitle("tests_2_10"),
                verifyText("//span[@id=\"simple\"]", "hello TestTag")
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

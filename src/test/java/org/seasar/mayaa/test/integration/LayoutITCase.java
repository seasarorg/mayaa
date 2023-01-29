package org.seasar.mayaa.test.integration;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;
public class LayoutITCase extends WebDriverBase {
    //@formatter:off
    final static Stream<Arguments> data = Stream.of(
        arguments(
            "simple",
            "/tests/layout/basepage1.html",
            new Command[] {
                verifyTitle("tests_5_01"),
                verifyElementPresent("//div[@id=\"slot\"]"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "basepage value"),
                verifyTextNotPresent("dummy")
            }
        ),
        arguments(
            "relative",
            "/tests/layout/relative/basepage2.html",
            new Command[] {
                verifyTitle("tests_5_02"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "basepage value")
            }
        ),
        arguments(
            "usecomponent.html",
            "/tests/layout/usecomponent.html",
            new Command[] {
                verifyTitle("tests_5_03"),
                verifyText("//div[@class=\"uc1\"]/div[@class=\"c1\"]", "component"),
                verifyText("//div[@class=\"uc1\"]/div[@class=\"c2\"]", "component"),
                verifyText("//div[@class=\"uc1\"]/div[@class=\"c3\"]", "component"),
                verifyText("//div[@class=\"uc2\"]/div[@class=\"c1\"]", "component"),
                verifyText("//div[@class=\"uc2\"]/div[@class=\"c2\"]", "component"),
                verifyText("//div[@class=\"uc2\"]/div[@class=\"c3\"]", "component"),
                verifyText("//div[@class=\"uc3\"]/div[@class=\"c1\"]", "component"),
                verifyText("//div[@class=\"uc3\"]/div[@class=\"c2\"]", "component"),
                verifyText("//div[@class=\"uc3\"]/div[@class=\"c3\"]", "component")
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

package org.seasar.mayaa.test.integration;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LayoutITCase extends WebDriverBase {
    //@formatter:off
    final static Object[][] data = new Object[][] {
        /* TestLayout */
        {
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
        }, {
            "relative",
            "/tests/layout/relative/basepage2.html",
            new Command[] {
                verifyTitle("tests_5_02"),
                verifyElementPresent("//div[@id=\"centered\"]"),
                verifyElementPresent("//div[@id=\"centered\"]/div"),
                verifyText("//div[@id=\"centered\"]/div", "basepage value")
            }
        }, {
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
        }
    };
    //@formatter:on

    @Parameters(name = "{0}: path={1}")
    public static Iterable<Object[]> data() throws Throwable {
        return Arrays.asList(data);
    }

    public LayoutITCase(String title, String path, Command[] commands) {
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

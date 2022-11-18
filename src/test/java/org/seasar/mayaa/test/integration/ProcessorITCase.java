package org.seasar.mayaa.test.integration;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ProcessorITCase extends WebDriverBase {
    //@formatter:off
    final static Object[][] data = new Object[][] {
        {
            "write",
            "/tests/processor/write.html",
            new Command[] {
                verifyTitle("tests_3_01"),
                verifyTextPresent("Test1st"),
                verifyTextPresent("Test2nd"),
                verifyTextPresent("Test3rd"),
                // test4: &amp;Test4th verifyTextPresentは実体参照を解決した後の文字で比較するテスト仕様。一方、verifyTextはinnerHTMLで比較する。
                verifyTextPresent("&Test4th"),
                verifyTextPresent("Test5th"),
                verifyTextNotPresent("$"),
                /* しっかりテストできていない。どうするか。 */
                verifyText("//span[@id=\"test6\"]", "\"<b>&"),
                // test7, test8: innerHTMLとして取得すると &quot; は実体参照とならない
                verifyText("//span[@id=\"test7\"]", "\"&lt;b&gt;&amp;"),
                verifyText("//span[@id=\"test8\"]", "\"&lt;b&gt;&amp;"),
                verifyText("//span[@id=\"test9\"]", "<br><br><br>" /* <br /><br /><br /> */),
                verifyText("//span[@id=\"test10\"]", ""),
                // test11: innerHTMLとして取得すると実体参照とならず、さらに &#xd; (キャリッジリターン)は文字として取得できない。
                verifyText("//span[@id=\"test11\"]", " \t\n" /* &#x9;&#xd;&#xa; */),                
                verifyText("//span[@id=\"test12\"]", "&amp;Test12th"), 
                verifyTextNotPresent("dummy"),
                verifyText("//span[@id=\"test13\"]", "123bodies and bodies321"),
                verifyText("//span[@id=\"test14\"]", "%{like template}"),
                verifyText("//span[@id=\"test15\"]", "100"),
                verifyText("//span[@id=\"test16\"]", "&amp;Test16th"),
                verifyText("//span[@id=\"test17\"]", "body<br>text<br>to<br>multi<br>line")
            }
        }, {
            "if",
            "/tests/processor/if.html",
            new Command[] {
                verifyTitle("tests_3_02"),
                verifyTextPresent("if_1"),
                verifyTextNotPresent("if_2"),
                verifyTextPresent("if_3"),
                verifyTextNotPresent("if_4"),
                verifyTextPresent("if_5"),
                verifyTextNotPresent("if_6")
            }
        }, {
            "for",
            "/tests/processor/for.html",
            new Command[] {
                verifyTitle("tests_3_03"),
                verifyTextPresent("test1_0"),
                verifyTextPresent("test1_1"),
                verifyTextPresent("test1_2"),
                verifyTextNotPresent("test2_0"),
                verifyTextPresent("test3_0"),
                verifyTextPresent("test3_1"),
                verifyTextPresent("test3_2")
            }
        }, {
            "for too many loop",
            "/tests/processor/for_toomany.html",
            new Command[] {
                verifyTextPresent("Too many loops, max count is 2")
            }
        }, {
            "forEach",
            "/tests/processor/forEach.html",
            new Command[] {
                verifyTitle("tests_3_05"), verifyTextPresent("0:1"),
                verifyTextPresent("1:foo"),
                verifyTextPresent("2:bar"),
                verifyTextNotPresent("dummy")
            }
        }, {
            "formatNumber",
            "/tests/processor/formatNumber.html",
            new Command[] {
                verifyTitle("tests_3_06"),
                verifyTextPresent("1,234.5600"),
                verifyTextNotPresent("dummy"),
                verifyTextPresent("defValue"),
                verifyTextNotPresent("error")
            }
        }, {
            "formatDate",
            "/tests/processor/formatDate.html",
            new Command[] {
                verifyTitle("tests_3_07"),
                verifyTextPresent("1999/07/07 12:34:56"),
                verifyTextNotPresent("dummy"),
                verifyTextPresent("defValue"),
                verifyTextNotPresent("error")
            }
        }, {
            "element",
            "/tests/processor/element.html",
            new Command[] {
                verifyTitle("tests_3_08"),
                verifyElementPresent("//h1"),
                verifyAttribute("//span[@id=\"test2\"]", "class", "processing"),
                verifyAttribute("//span[@id=\"test3\"]", "class", "\"&copy;<foo>'"),
                verifyAttribute("//span[@id=\"test4\"]", "class", "\"©<foo>'"),
                verifyTextPresent("element1"),
                verifyTextPresent("element2")
            }
        }, {
            "echo",
            "/tests/processor/echo.html",
            new Command[] {
                verifyTitle("tests_3_09"),
                verifyElementPresent("//span[@id=\"test1\"]"),
                verifyElementPresent("//span[@class=\"processing\"]"),
                verifyElementPresent("//span[@class=\"processing2\"]"),
                verifyElementNotPresent("//span[@id=\"test3\"]"),
                verifyElementPresent("//a[@id=\"test4\"]"),
                verifyAttribute("//a[@id=\"test4\"]", "href", "regexp:.*(/[^/]+)?/index.html"),
                verifyTextPresent("echo1"), verifyTextPresent("echo2"),
                verifyTextPresent("echo3")
            }
        }, {
            "comment",
            "/tests/processor/comment.html",
            new Command[] {
                verifyTitle("tests_3_10"),
                verifyElementNotPresent("//span[@id=\"commentOut\"]"),
                verifyTextNotPresent("body"),
                verifyTextNotPresent("comment out by mayaa")
            }
        }, {
            "forEach recursive",
            "/tests/processor/forEachRecursive.html",
            new Command[] {
                verifyTitle("tests_3_11"),
                verifyText("//ul[@id='root']/li[1]/span", "1"),
                verifyText("//ul[@id='root']/li/ul/li[1]/span", "1-1"),
                verifyText("//ul[@id='root']/li/ul/li[2]/span", "1-2"),
                verifyText("//ul[@id='root']/li/ul/li[3]/span", "1-3"),
                verifyText("//ul[@id='root']/li/ul/li/ul/li[1]/span", "1-3-1"),
                verifyText("//ul[@id='root']/li/ul/li/ul/li[2]/span", "1-3-2"),
                verifyText("//ul[@id='root']/li/ul/li/ul/li[3]/span", "1-3-3"),
                verifyText("//ul[@id='root']/li/ul/li/ul/li/ul/li[1]/span", "1-3-3-1"),
                verifyTextNotPresent("dummy")
            }
        }, {
            "exec",
            "/tests/processor/exec.html",
            new Command[] {
                verifyTitle("tests_3_12"),
                verifyElementNotPresent("//span[@id=\"commentOut\"]"),
                verifyText("test1", "テスト1"),
                verifyText("test2", "テスト2"),
                verifyText("test3", "テスト3"),
                verifyText("test4", "テスト4")
            }
        }
    };
    //@formatter:on

    static List<Object[]> test() throws Throwable {
    @BeforeAll
    public static void setUpClass() {
        setUpSelenide();
    }

    @ParameterizedTest(name = "{0}: path={1}")
    @MethodSource
    public void test(String title, String path, Command[] commands) {
        runTest(title, path, commands);
    }

    static List<Object[]> test() {
        return Arrays.asList(data);
    }
}

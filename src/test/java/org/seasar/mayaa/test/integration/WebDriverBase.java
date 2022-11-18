package org.seasar.mayaa.test.integration;

import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byId;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.title;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.regex.Pattern;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Driver;
import org.openqa.selenium.WebElement;

public class WebDriverBase {

    static void setUpSelenide() {
        // テスト対象アプリの起動
        // Configuration.browser = WebDriverRunner.SAFARI;
        // Configuration.browser = WebDriverRunner.HTMLUNIT;
        Configuration.baseUrl = "http://localhost:8999";
        Configuration.reportsFolder = "target/selenide-reports";
    }

    public void runTest(String title, String path, Command[] commands) {
        open(path);
        if (commands == null) {
            fail("Scenario is not described.");
        }

        for (Command command : commands) {
            if (command instanceof VerifyTitle) {
                assertEquals(command.value, title());
            } else if (command instanceof VerifyText) {
                if (command.xpath.startsWith("/")) {
                    $x(command.xpath).shouldHave(innerHtml(command.value));
                } else {
                    $(byId(command.xpath)).shouldHave(innerHtml(command.value));
                }
            } else if (command instanceof VerifyTextPresent) {
                $(withText(command.value)).should(exist);
            } else if (command instanceof VerifyTextNotPresent) {
                $(withText(command.value)).shouldNot(exist);
            } else if (command instanceof VerifyAttribute) {
                $x(command.xpath)
                        .should(attributeEx(((VerifyAttribute) command).attr, command.value));
            } else if (command instanceof VerifyElementPresent) {
                $x(command.xpath).should(exist);
            } else if (command instanceof VerifyElementNotPresent) {
                $x(command.xpath).shouldNot(exist);
            }
        }
    }

    /**
     * 接頭辞として exact: や regexp: が入っている比較に応じてテキスト比較を行うための適切な Condition を返却する。
     * 
     * @param value 比較対象の文字列
     */
    Condition textEx(String value) {
        if (value.startsWith("exact:")) {
            return exactText(value.substring("exact:".length()));
        } else if (value.startsWith("regexp:")) {
            return matchText(value.substring("regexp:".length()));
        } else {
            return text(value);
        }
    }

    /**
     * 接頭辞として exact: や regexp: が入っている比較に応じて属性値の比較を行うConditionを返却する。
     * 
     * @param attrName      対象とする属性の名称
     * @param expectedValue 比較対象の文字列
     * @return Condtionオブジェクト
     */
    Condition attributeEx(final String attrName, final String expectedValue) {
        return new Condition("attributeEx") {
            @Override
            public boolean apply(Driver driver, WebElement element) {
                final String actualText = element.getAttribute(attrName);
                final String expectedText = expectedValue;

                if (expectedText.startsWith("exact:")) {
                    final String text = expectedText.substring("exact:".length());
                    return text.equals(actualText);
                } else if (expectedText.startsWith("regexp:")) {
                    final String regexp = expectedText.substring("regexp:".length());
                    return Pattern.matches(regexp, actualText);
                } else {
                    return actualText.contains(expectedText);
                }
            }

            @Override
            public String toString() {
                return String.format("have attribute %s with \"%s\"", attrName, expectedValue);
            }
        };
    }

    /**
     * 接頭辞として exact: や regexp: が入っている比較に応じて属性値の比較を行うConditionを返却する。
     * 
     * @param attrName      対象とする属性の名称
     * @param expectedValue 比較対象の文字列
     * @return Condtionオブジェクト
     */
    Condition innerHtml(final String expectedValue) {
        return new Condition("innerHtml") {
            @Override
            public boolean apply(Driver driver, WebElement element) {
                final String actualText = element.getAttribute("innerHTML");
                final String expectedText = expectedValue;

                if (expectedText.startsWith("exact:")) {
                    final String text = expectedText.substring("exact:".length());
                    return text.equals(actualText);
                } else if (expectedText.startsWith("regexp:")) {
                    final String regexp = expectedText.substring("regexp:".length());
                    return Pattern.matches(regexp, actualText);
                } else {
                    return actualText.contains(expectedText);
                }
            }

            @Override
            public String actualValue(Driver driver, WebElement element) {
                final String actualText = element.getAttribute("innerHTML");

                StringBuilder builder = new StringBuilder();
                builder.append("length=").append(actualText.length()).append(", \"");
                for (int i = 0; i < actualText.length(); ++i) {
                    int codePoint = actualText.codePointAt(i);
                    if (Character.isISOControl(codePoint)) {
                        builder.append("\\u").append(Integer.toHexString(codePoint));
                    } else {
                        builder.appendCodePoint(codePoint);
                    }
                }
                builder.append("\"");
                return builder.toString();
            }

            @Override
            public String toString() {
                return String.format("have innerHTML with \"%s\"", expectedValue);
            }
        };
    }

    /**
     * SeleniuｍIDEで出力、保村ざれたシナリオの各コマンドに該当する抽象クラス。 具体的なコマンド名を継承して作成し、static 関数を使って生成するようにすることで
     * JUnitのParameterizedテストのデータ定義を簡便化する。 対象のエレメントはXPath形式で指定する。
     */
    abstract static class Command {
        String xpath;
        String value;
    }

    static class VerifyTitle extends Command {
    }

    static class VerifyText extends Command {
    }

    static class VerifyTextPresent extends Command {
    }

    static class VerifyTextNotPresent extends Command {
    }

    static class VerifyAttribute extends Command {
        /** 比較の対象となるエレメントの属性名 */
        String attr;
    }

    static class VerifyElementPresent extends Command {
    }

    static class VerifyElementNotPresent extends Command {
    }

    static VerifyTitle verifyTitle(String value) {
        VerifyTitle r = new VerifyTitle();
        r.value = value;
        return r;
    }

    static VerifyText verifyText(String xpath, String value) {
        VerifyText r = new VerifyText();
        r.xpath = xpath;
        r.value = value;
        return r;
    }

    static VerifyTextPresent verifyTextPresent(String value) {
        VerifyTextPresent r = new VerifyTextPresent();
        r.value = value;
        return r;
    }

    static VerifyTextNotPresent verifyTextNotPresent(String value) {
        VerifyTextNotPresent r = new VerifyTextNotPresent();
        r.value = value;
        return r;
    }

    static VerifyAttribute verifyAttribute(String xpath, String attr, String value) {
        VerifyAttribute r = new VerifyAttribute();
        r.xpath = xpath;
        r.value = value;
        r.attr = attr;
        return r;
    }

    static VerifyElementPresent verifyElementPresent(String xpath) {
        VerifyElementPresent r = new VerifyElementPresent();
        r.xpath = xpath;
        return r;
    }

    static VerifyElementNotPresent verifyElementNotPresent(String xpath) {
        VerifyElementNotPresent r = new VerifyElementNotPresent();
        r.xpath = xpath;
        return r;
    }

}

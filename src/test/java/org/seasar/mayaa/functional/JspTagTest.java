package org.seasar.mayaa.functional;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class JspTagTest extends EngineTestBase {

    @Test
    public void JSPカスタムタグのcoutで固定メッセージを出力する() throws IOException {
        execAndVerify("/it-case/html-transform/jsp-tags/target.html", "/it-case/html-transform/jsp-tags/expected.html", null);
    }

}

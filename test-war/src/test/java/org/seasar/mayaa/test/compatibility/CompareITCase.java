package org.seasar.mayaa.test.compatibility;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class CompareITCase {

    final String[] pathArray = new String[] {
        "/tests/engine/inject_no.html",
        "/tests/engine/inject_mayaa.html",
        "/tests/engine/inject_template.html",
        "/tests/engine/inject_xpath.html",
        "/tests/engine/replace.html",
        "/tests/engine/ignore_mayaa.html",
        "/tests/engine/xout.html",
        "/tests/engine/escape.html",
        "/tests/engine/escape.xhtml",
        "/tests/engine/escape.xml",
        "/tests/engine/beforeRender.html",
        "/tests/engine/template_attribute.html",
        "/tests/engine/script.html",
        "/tests/engine/no_xml.html",
        "/tests/engine/forward/forward.html",
        "/tests/engine/forward/redirect.html",
        "/tests/engine/forward/error_forward.html",
        "/tests/engine/forward/error_redirect.html",
        "/tests/engine/required.html",
        "/tests/engine/required_not_empty.html",
        "/tests/engine/undefined_identifier.html",
        "/tests/engine/cdata.html",
        "/tests/engine/forward/forward_to_webinf.html",
        "/tests/engine/charset_html.html",
        "/tests/engine/charset_xhtml.html",
        "/tests/engine/inject_include.html",
        "/tests/component/component1_client.html",
        "/tests/component/component1.html",
        "/tests/component/component2_client.html",
        "/tests/component/relative/component3_client.html",
        "/tests/component/recursive.html",
        "/tests/component/binding.html",
        "/tests/component/binding_recursive.html",
        "/tests/component/component123_client.html",
        "/tests/component/component3_client.html",
        "/tests/component/component3.html",
        "/tests/processor/write.html",
        "/tests/processor/if.html",
        "/tests/processor/for.html",
        "/tests/component/relative/component3_client.html",
        "/tests/component/recursive.html",
        "/tests/component/binding.html",
        "/tests/component/binding_recursive.html",
        "/tests/component/component123_client.html",
        "/tests/component/component3_client.html",
        "/tests/component/component3.html",
        "/tests/processor/write.html",
        "/tests/processor/if.html",
        "/tests/processor/for.html",
        "/tests/processor/for_toomany.html",
        "/tests/processor/forEach.html",
        "/tests/processor/formatNumber.html",
        "/tests/processor/formatDate.html",
        "/tests/processor/element.html",
        "/tests/processor/echo.html",
        "/tests/processor/comment.html",
        "/tests/processor/forEachRecursive.html",
        "/tests/processor/exec.html",
        "/tests/customtag/scopetest.html",
        "/tests/customtag/eval_body_buffered.html",
        "/tests/customtag/simpletest.html",
        "/tests/customtag/dynamic_attribute.html",
        "/tests/customtag/dynamic_attribute_wrong.html",
        "/tests/customtag/rtexprtest.html",
        "/tests/customtag/simple_dynamic_test.html",
        "/tests/customtag/vinulltest.html",
        "/tests/customtag/simplebodytest.html",
        "/tests/customtag/emptybody.html",
        "/tests/layout/basepage1.html",
        "/tests/layout/relative/basepage2.html",
        "/tests/layout/usecomponent.html"
        // "/tests/engine/through.html",   // Empty string の出力形式、Void Element対応により差分あり
       // "/tests/customtag/replace_injection_attribute.html",  // v1.1.34にてなぜか閉じタグ</html> が </xml:html> となっている。
    };

    @Test
    // @EnabledIfEnvironmentVariable(named = "ffff", matches = "1")
    public void runTest() {
        final String version_1 = System.getProperty("VERSION_1", "1.1.34");
        final String version_2 = System.getProperty("VERSION_2", "1.2.1-SNAPSHOT");

        String[] versions = new String[] { version_1, version_2 };
        for (String path: pathArray) {
            List<List<String>> results = new ArrayList<>();

            for (int i = 0; i < versions.length; ++i) {
                try {
                    final String context = "/mayaa-" + versions[i];
                    URL url = new URL("http", "localhost", 8080, context + path);
                    URLConnection connection = url.openConnection();

                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    BufferedReader br = new BufferedReader(reader);

                    // PathAdjusterが動作した部分を吸収
                    results.add(br
                      .lines()
                      .map(e -> e.replace(context, "/MAYAA"))
                      .collect(Collectors.toList()));

                      // System.out.println(results.get(i));

                } catch (MalformedURLException e) {
                    System.err.println(path);
                } catch (IOException e) {
                    System.err.println(path);
                } finally {
                    //
                }
            }
            if (results.size() == 2) {
                assertArrayEquals(results.get(0).toArray(), results.get(1).toArray(), path);
            }
        }
    }
}

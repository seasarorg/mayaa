package org.seasar.mayaa.functional;

import java.io.IOException;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


public class EngineTest extends EngineTestBase {

    @Test
    public void 単純ケース() throws IOException {
        // Given
        getServletContext().setAttribute("s0", "sv0");

        MockHttpServletRequest request = createRequest("/it-case/engine/test.html");
        request.setAttribute("p0", "v0");

        // When
        MockHttpServletResponse response = exec(request, null);
        
        // Then
        verifyResponse(response, "/it-case/engine/expected.html");
    }

}

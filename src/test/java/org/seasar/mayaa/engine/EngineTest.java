package org.seasar.mayaa.engine;

import java.io.IOException;

import org.junit.Test;


public class EngineTest extends EngineTestBase {

    @Test
    public void 単純ケース() throws IOException {
        // Given
        request.setAttribute("p0", "v0");
        
        // When
        exec("/it-case/engine/test.html", null);
        
        // Then
        verifyResponse("/it-case/engine/expected.html");
    }
}

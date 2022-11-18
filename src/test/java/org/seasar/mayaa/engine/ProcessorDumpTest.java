package org.seasar.mayaa.engine;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.functional.EngineTestBase;
import org.seasar.mayaa.impl.engine.ProcessorDump;
import org.springframework.mock.web.MockHttpServletRequest;

public class ProcessorDumpTest extends EngineTestBase {
    static final String BASE_PATH = "/it-case/html-transform/processors/";

    @Test
    public void プロセッサのダンプができるcomponent() {
        final MockHttpServletRequest request = createRequest(BASE_PATH + "component/hello.html");
        exec(request, null);
        
        ProcessorDump dump = new ProcessorDump();
        dump.setPrintContents(false);

        Page page = getPage();

        dump.printSource(page);
    }

    @Test
    public void プロセッサのダンプができるfor() {
        final MockHttpServletRequest request = createRequest(BASE_PATH + "for/target.html");
        exec(request, null);
        
        ProcessorDump dump = new ProcessorDump();
        dump.setPrintContents(false);
        dump.setIndentChar("~~");

        Page page = getPage();

        dump.printSource(page);
    }

    @Test
    public void プロセッサのダンプができるif() {
        final MockHttpServletRequest request = createRequest(BASE_PATH + "if/target.html");
        exec(request, null);
        
        ProcessorDump dump = new ProcessorDump();
        dump.setPrintContents(false);

        Page page = getPage();

        dump.printSource(page);
    }

}
package org.seasar.mayaa.impl.engine.specification.serialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.engine.PageImpl;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.source.SourceUtil;
import org.seasar.mayaa.test.util.ManualProviderFactory;

public class SerializeExecutorTest {

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
    }

    @Test
    public void testSerializeReserve() throws InterruptedException, IOException {
        Path tmpDir = Files.createTempDirectory("mayaa-ut");
        ManualProviderFactory.SERVLET_CONTEXT.setAttribute("javax.servlet.context.tempdir", tmpDir.toFile());
        SerializeExecutor testee = new SerializeExecutor();

        Page spec = new PageImpl();

        spec.setSource(SourceUtil.getSourceDescriptor("/page.mayaa"));
        spec.initialize("/page");
        spec.build();

        testee.submit(spec);

        Thread.sleep(500);
        Specification actual = SpecificationUtil.deserialize(spec.getSystemID());
        assertTrue(actual instanceof Page, "Instance must be of Page");
        assertEquals(spec, actual);

        assertFalse(actual.isDeprecated());

        tmpDir.toFile().delete();
    }

}

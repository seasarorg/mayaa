/*
 * Copyright 2004-2011 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.engine.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.FactoryFactory;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.impl.FactoryFactoryImpl;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupportTest {

    private ServletContext _servletContext;
    private TemplateProcessorSupport _processor;
    private TemplateProcessorSupport _parent;

    @BeforeEach
    public void setUp() throws Exception {
        _servletContext = new MockServletContext();
        FactoryFactory.setInstance(new FactoryFactoryImpl());
        FactoryFactory.setContext(_servletContext);

        _parent = new TemplateProcessorSupportTestee();
        _processor = new TemplateProcessorSupportTestee();
        _processor.setParentProcessor(_parent);
    }

    @Test
    public void testAddChild() throws Exception {
        // addChild(TemplateProcessor)にて追加した子プロセッサのリストが、
        // getChildren()で配列として取得できる。
        TemplateProcessorSupport child = new TemplateProcessorSupportTestee();
        _processor.addChildProcessor(child);
        assertEquals(1, _processor.getChildProcessorSize());
        assertEquals(child, _processor.getChildProcessor(0));
        assertEquals(_processor, child.getParentProcessor());
        try {
            // addChild(TemplateProcessor)にて引数nullを渡すと、
            // NullPointerException発生。
            _processor.addChildProcessor(null);
            fail();
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testGetParent() throws Exception {
        // init(..., TemplateProcessor, ...)にて設定した親プロセッサが、
        // getParent()で取得できる。
        assertEquals(_parent, _processor.getParentProcessor());
    }

    @Test
    public void testDoStartProcess() throws Exception {
        // doStartProcess(MayaaContext)のデフォルトの返値は、EVAL_BODY_INCLUDEとなる。
        assertEquals(ProcessStatus.EVAL_BODY_INCLUDE, _processor.doStartProcess(null));
    }

    @Test
    public void testDoEndProcess() throws Exception {
        // doEndProcess(MayaaContext)のデフォルトの返値は、EVAL_PAGEとなる。
        assertEquals(ProcessStatus.EVAL_PAGE, _processor.doEndProcess());
    }

}

class TemplateProcessorSupportTestee extends TemplateProcessorSupport {

    private static final long serialVersionUID = -1303874983746243643L;
    
}

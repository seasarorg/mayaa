/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License"); you may
 * not use this file except in compliance with the License which accompanies
 * this distribution, and is available at
 * 
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.seasar.maya.engine.processor;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateProcessorSupportTest extends TestCase {

    private TemplateProcessorSupport _processor;
    private TemplateProcessorSupport _parent;
    
    public TemplateProcessorSupportTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        _parent = new TemplateProcessorSupport();
        _processor = new TemplateProcessorSupport();
        _processor.setParentProcessor(_parent, 0);
    }

    public void testAddChild() throws Exception {
        // addChild(TemplateProcessor)にて追加した子プロセッサのリストが、
        // getChildren()で配列として取得できる。
        TemplateProcessorSupport child = new TemplateProcessorSupport();
        _processor.addChildProcessor(child);
        assertEquals(1, _processor.getChildProcessorSize());
        assertEquals(child, _processor.getChildProcessor(0));
        assertEquals(_processor, child.getParentProcessor());
        assertEquals(0, child.getIndex());
        try {
            // addChild(TemplateProcessor)にて引数nullを渡すと、
            // NullPointerException発生。
            _processor.addChildProcessor(null);
            fail();
        } catch(IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testGetParent() throws Exception {
        // init(..., TemplateProcessor, ...)にて設定した親プロセッサが、
        // getParent()で取得できる。
        assertEquals(_parent, _processor.getParentProcessor());
    }

    public void testDoStartProcess() throws Exception {
        // doStartProcess(MayaContext)のデフォルトの返値は、EVAL_BODY_INCLUDEとなる。
//        TestPageContext context = new TestPageContext();
//        assertEquals(Tag.EVAL_BODY_INCLUDE, _processor.doStartProcess(context));
    }

    public void testDoEndProcess() throws Exception {
        // doEndProcess(MayaContext)のデフォルトの返値は、EVAL_PAGEとなる。
//        TestPageContext context = new TestPageContext();
//        assertEquals(Tag.EVAL_PAGE, _processor.doEndProcess(context));
    }

}

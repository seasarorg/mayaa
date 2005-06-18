/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.engine;

import junit.framework.TestCase;

import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.processor.ElementProcessor;

/**
 * @author maruo_syunsuke
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class TemplateImplTest extends TestCase implements CONST_IMPL {
    
	private EngineImpl _engine;
    private PageImpl _page;
    private TemplateImpl _template;
    
    protected void setUp() {
        _engine = new EngineImpl();
        _page = new PageImpl(_engine, "hello", "html");
        _template = new TemplateImpl(_page, "ja");
        ElementProcessor html = new ElementProcessor();
        html.setQName(new QName(URI_HTML, "html"));
        _template.addChildProcessor(html);
        ElementProcessor body = new ElementProcessor();
        body.setQName(new QName(URI_HTML, "body"));
        html.addChildProcessor(body);
    }
    
    public void testGetSuffix() {
        assertEquals("ja", _template.getSuffix());
    }
    
    public void testGetPage() {
        assertEquals(_page, _template.getPage());
    }

}

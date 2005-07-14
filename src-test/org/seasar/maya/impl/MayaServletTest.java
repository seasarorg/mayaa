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
package org.seasar.maya.impl;

import junit.framework.TestCase;

import org.seasar.maya.impl.provider.EngineSettingImpl;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MayaServletTest extends TestCase {
    
    private MayaServlet _servlet;
    
    public MayaServletTest(String name) {
        super(name);
    }
    
    protected void setUp() {
        _servlet = new MayaServlet();
    }
    
    public void testGetRequestedPageInfo() {
        String[] info;
        // よくあるパターン
        EngineSettingImpl setting = new EngineSettingImpl();
        info = _servlet.getRequestedPageInfo(setting, "/index.html");
        assertEquals("/index", info[0]);
        assertEquals("", info[1]);
        assertEquals("html", info[2]);
        
        // 接尾辞を強制的に指定されているパターン
        info = _servlet.getRequestedPageInfo(setting, "/index$ja.html");
        assertEquals("/index", info[0]);
        assertEquals("ja", info[1]);
        assertEquals("html", info[2]);

        // フォルダ階層があるパターン
        info = _servlet.getRequestedPageInfo(setting, "/folder/index.html");
        assertEquals("/folder/index", info[0]);
        assertEquals("", info[1]);
        assertEquals("html", info[2]);
        
        // 拡張子がないパターン
        info = _servlet.getRequestedPageInfo(setting, "/index");
        assertEquals("/index", info[0]);
        assertEquals("", info[1]);
        assertEquals("", info[2]);
        
        // ファイル名の先頭がドットのパターン
        info = _servlet.getRequestedPageInfo(setting, "/.index");
        assertEquals("/.index", info[0]);
        assertEquals("", info[1]);
        assertEquals("", info[2]);

        // ファイル名の先頭がアンダースコアのパターン
        info = _servlet.getRequestedPageInfo(setting, "/_index.html");
        assertEquals("/_index", info[0]);
        assertEquals("", info[1]);
        assertEquals("html", info[2]);
    }

}

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
package org.seasar.maya.impl.cycle.script;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockTest extends TestCase {

    public ScriptBlockTest(String name) {
        super(name);
    }

    public void testGetBlockString1() {
        // 式文字列中のエンティティ解決
        ScriptBlock block = new ScriptBlock("&amp;", false);
        assertFalse(block.isLiteral());
        assertEquals("&", block.getBlockString());
    }

    public void testGetBlockString2() {
        // テキストボディ中のエンティティ解決
        ScriptBlock block = new ScriptBlock("&amp;", true);
        assertTrue(block.isLiteral());
        assertEquals("&amp;", block.getBlockString());
    }

    public void testGetBlockString3() {
        // エンティティの閉じセミコロンがないとき
        ScriptBlock block = new ScriptBlock("&amp", false);
        assertEquals("&amp", block.getBlockString());
    }

    public void testGetBlockString4() {
        // エンティティの閉じセミコロンと開き＆の関係がおかしいとき
        ScriptBlock block = new ScriptBlock(";amp&", false);
        assertEquals(";amp&", block.getBlockString());
    }

    public void testGetBlockString5() {
        // 架空のエンティティ名のとき
        ScriptBlock block = new ScriptBlock("&abcxyz;", false);
        assertEquals("&abcxyz;", block.getBlockString());
    }

    public void testGetBlockString6() {
        // エンティティが複数含まれるとき
        ScriptBlock block = new ScriptBlock("&lt;&gt;", false);
        assertEquals("<>", block.getBlockString());
    }

    public void testGetBlockString7() {
        // エンティティが複数と間に字があるとき
        ScriptBlock block = new ScriptBlock("&lt;a&gt;", false);
        assertEquals("<a>", block.getBlockString());
    }

    public void testGetBlockString8() {
        // エンティティが複数と前に字があるとき
        ScriptBlock block = new ScriptBlock("&lt;a", false);
        assertEquals("<a", block.getBlockString());
    }

    public void testGetBlockString9() {
        // エンティティが複数と後に字があるとき
        ScriptBlock block = new ScriptBlock("a&gt;", false);
        assertEquals("a>", block.getBlockString());
    }
    
}

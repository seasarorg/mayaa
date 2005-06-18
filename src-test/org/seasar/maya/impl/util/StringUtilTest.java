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
package org.seasar.maya.impl.util;

import junit.framework.TestCase;

/**
 * @author suga
 */
public class StringUtilTest extends TestCase {

    public StringUtilTest(String name) {
        super(name);
    }

    public void testResolveEntity() {
        // 解決するエンティティのとき
        assertEquals("&", StringUtil.resolveEntity("&amp;"));
        assertEquals("<", StringUtil.resolveEntity("&lt;"));
        assertEquals(">", StringUtil.resolveEntity("&gt;"));
        assertEquals("\"", StringUtil.resolveEntity("&quot;"));

        // 架空のエンティティ名のとき
        assertEquals("&abcdefg;", StringUtil.resolveEntity("&abcdefg;"));

        // エンティティの閉じセミコロンがないとき
        assertEquals("&amp", StringUtil.resolveEntity("&amp"));

        // 複数のとき
        assertEquals("<\"&>", StringUtil.resolveEntity("&lt;&quot;&amp;&gt;"));

        // エンティティの前に字があるとき
        assertEquals("foo>", StringUtil.resolveEntity("foo&gt;"));

        // エンティティの後に字があるとき
        assertEquals("<foo", StringUtil.resolveEntity("&lt;foo"));

        // エンティティの間に字があるとき
        assertEquals("<foo>", StringUtil.resolveEntity("&lt;foo&gt;"));

        // エンティティの前後と間に字があるとき
        assertEquals("foo<bar>baz",
                StringUtil.resolveEntity("foo&lt;bar&gt;baz"));
    }
    
}

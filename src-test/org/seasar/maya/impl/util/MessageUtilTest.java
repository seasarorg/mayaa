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
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MessageUtilTest extends TestCase {

    public MessageUtilTest(String name) {
        super(name);
    }
    
    public void testGetMessage() {
        String message = MessageUtil.getMessage(MessageKey.class, 0);
        assertEquals("test message", message);
        String message1 = MessageUtil.getMessage(MessageKey.class, 1);
        assertEquals("test message 1", message1);
        String message2 = MessageUtil.getMessage(MessageKey.class, 2);
        assertEquals("test message 2", message2);
    }
    
}

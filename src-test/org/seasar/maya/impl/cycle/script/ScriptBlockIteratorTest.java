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

import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockIteratorTest extends TestCase {

    public ScriptBlockIteratorTest(String name) {
        super(name);
    }
    
    public void testNext1() {
    	Iterator it = new ScriptBlockIterator("${ 123 } 456 ${ 789 }", "$");
    	assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
    	assertFalse(block1.isLiteral());
    	assertEquals(" 123 ", block1.getBlockString());
    	assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
    	assertTrue(block2.isLiteral());
    	assertEquals(" 456 ", block2.getBlockString());
    	assertTrue(it.hasNext());
        ScriptBlock block3 = (ScriptBlock)it.next();
    	assertFalse(block3.isLiteral());
    	assertEquals(" 789 ", block3.getBlockString());
    	try {
    		it.next();
    		fail();
    	} catch(NoSuchElementException e) {
    	}
    }

    public void testNext2() {
    	Iterator it = new ScriptBlockIterator("123 ${ 456 } 789", "$");
    	assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
    	assertTrue(block1.isLiteral());
    	assertEquals("123 ", block1.getBlockString());
    	assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
    	assertFalse(block2.isLiteral());
    	assertEquals(" 456 ", block2.getBlockString());
    	assertTrue(it.hasNext());
        ScriptBlock block3 = (ScriptBlock)it.next();
    	assertTrue(block3.isLiteral());
    	assertEquals(" 789", block3.getBlockString());
        assertFalse(it.hasNext());
    }

    public void testNext3() {
        Iterator it = new ScriptBlockIterator(
                "${ obj = { run: function() { return 'hi'; } }; }", "$");
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertFalse(block1.isLiteral());
        assertEquals(" obj = { run: function() { return 'hi'; } }; ", block1.getBlockString());
        assertFalse(it.hasNext());
    }
    
}

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
package org.seasar.mayaa.impl.cycle.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptBlockIteratorTest {

    @Test
    public void testNext1() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "${ 123 } 456 ${ 789 }", "$", false);
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
            // do nothing.
        }
    }

    @Test
    public void testNext2() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "123 ${ 456 } 789", "$", false);
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

    @Test
    public void testNext3() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "${ obj = { run: function() { return 'hi'; } }; }", "$", false);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertFalse(block1.isLiteral());
        assertEquals(" obj = { run: function() { return 'hi'; } }; ", block1.getBlockString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext4() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                " &lt; &amp; &gt; ${ &lt; &amp; &gt; }", "$", false);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertTrue(block1.isLiteral());
        assertEquals(" &lt; &amp; &gt; ", block1.getBlockString());
        assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
        assertFalse(block2.isLiteral());
        assertEquals(" &lt; &amp; &gt; ", block2.getBlockString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext5() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                " &lt; &amp; &gt; ${ &lt; &amp; &gt; }", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertTrue(block1.isLiteral());
        assertEquals(" &lt; &amp; &gt; ", block1.getBlockString());
        assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
        assertFalse(block2.isLiteral());
        assertEquals(" < & > ", block2.getBlockString());
        assertFalse(it.hasNext());
    }

    /* brace in line comment */
    @Test
    public void testNext6() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "//123 ${ if () { \n456; // } \n }\n} 789//", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertTrue(block1.isLiteral());
        assertEquals("//123 ", block1.getBlockString());
        assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
        assertFalse(block2.isLiteral());
        assertEquals(" if () { \n456; // } \n }\n", block2.getBlockString());
        ScriptBlock block3 = (ScriptBlock)it.next();
        assertTrue(block3.isLiteral());
        assertEquals(" 789//", block3.getBlockString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext7() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "//123 ${ if () { \r\n456; // } \r\n }\r\n} 789//", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertTrue(block1.isLiteral());
        assertEquals("//123 ", block1.getBlockString());
        assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
        assertFalse(block2.isLiteral());
        assertEquals(" if () { \r\n456; // } \r\n }\r\n", block2.getBlockString());
        ScriptBlock block3 = (ScriptBlock)it.next();
        assertTrue(block3.isLiteral());
        assertEquals(" 789//", block3.getBlockString());
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext8() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "${ 456; //\n}", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertFalse(block1.isLiteral());
        assertEquals(" 456; //\n", block1.getBlockString());
        assertFalse(it.hasNext());
    }

    /* brace in block comment */
    @Test
    public void testNext9() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "/*123*/ ${ if () { \n456; /* } */ }\n} /*789*/", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertTrue(block1.isLiteral());
        assertEquals("/*123*/ ", block1.getBlockString());
        assertTrue(it.hasNext());
        ScriptBlock block2 = (ScriptBlock)it.next();
        assertFalse(block2.isLiteral());
        assertEquals(" if () { \n456; /* } */ }\n", block2.getBlockString());
        ScriptBlock block3 = (ScriptBlock)it.next();
        assertTrue(block3.isLiteral());
        assertEquals(" /*789*/", block3.getBlockString());
        assertFalse(it.hasNext());
    }

    /* Unbalance */
    /* cannot distinct from literal '}'
    @Test
    public void testNext10() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "${ test; }}", "$", true);
        assertTrue(it.hasNext());
        ScriptBlock block1 = (ScriptBlock)it.next();
        assertEquals(" test; ", block1.getBlockString());
        assertTrue(it.hasNext());
        try {
            it.next();
            fail("UnbalancedBraceException is not thrown");
        } catch (UnbalancedBraceException e) {
            assertTrue(true);
        } catch (IllegalStateException e) {
            // test environment
            assertTrue(true);
        }
    }
    */

    @Test
    public void testNext11() {
        Iterator<ScriptBlock> it = new ScriptBlockIterator(
                "${{ test; }", "$", true);
        assertTrue(it.hasNext());
        try {
            it.next();
            fail("UnbalancedBraceException is not thrown");
        } catch (UnbalancedBraceException e) {
            assertTrue(true);
        } catch (IllegalStateException e) {
            // test environment
            assertTrue(true);
        }
    }

}

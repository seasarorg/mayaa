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
package org.seasar.mayaa.impl.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
* @author Koji Suga (Gluegent Inc.)
*/
public class CharsetConverterTest {
    
    /**
    * Test method for
    * {@link org.seasar.mayaa.impl.engine.CharsetConverter#charsetToEncoding(java.lang.String)}.
    */
    @Test
    public void testCharsetToEncoding() {
        CharsetConverter.setEnabled(true);
        assertEquals("Windows-31J", CharsetConverter.charsetToEncoding("Shift_JIS"));
        assertEquals("Windows-31J", CharsetConverter.charsetToEncoding("shift_jis"));
        assertEquals("Windows-31J", CharsetConverter.charsetToEncoding("Windows-31J"));
        assertEquals("UTF-8", CharsetConverter.charsetToEncoding("UTF-8"));
        assertEquals("EUC-JP", CharsetConverter.charsetToEncoding("EUC-JP"));
        
        CharsetConverter.setEnabled(false);
        assertEquals("Shift_JIS", CharsetConverter.charsetToEncoding("Shift_JIS"));
        assertEquals("shift_jis", CharsetConverter.charsetToEncoding("shift_jis"));
    }
    
    /**
    * Test method for
    * {@link org.seasar.mayaa.impl.engine.CharsetConverter#encodingToCharset(java.lang.String)}.
    */
    @Test
    public void testEncodingToCharset() {
        CharsetConverter.setEnabled(true);
        assertEquals("Shift_JIS", CharsetConverter.encodingToCharset("Windows-31J"));
        assertEquals("Shift_JIS", CharsetConverter.encodingToCharset("windows-31j"));
        assertEquals("Shift_JIS", CharsetConverter.encodingToCharset("Shift_JIS"));
        assertEquals("UTF-8", CharsetConverter.encodingToCharset("UTF-8"));
        assertEquals("EUC-JP", CharsetConverter.encodingToCharset("EUC-JP"));
        
        CharsetConverter.setEnabled(false);
        assertEquals("Windows-31J", CharsetConverter.encodingToCharset("Windows-31J"));
        assertEquals("windows-31j", CharsetConverter.encodingToCharset("windows-31j"));
    }
    
    /**
    * Test method for
    * {@link org.seasar.mayaa.impl.engine.CharsetConverter#convertContentType(java.lang.String)}.
    */
    @Test
    public void testConvertContentType() {
        CharsetConverter.setEnabled(true);
        assertEquals("text/html; charset=Shift_JIS",
        CharsetConverter.convertContentType("text/html; charset=Windows-31J"));
        assertEquals("text/html;charset=Shift_JIS",
        CharsetConverter.convertContentType("text/html;charset=windows-31j"));
        assertEquals("text/html; charset=UTF-8", CharsetConverter.convertContentType("text/html"));
        assertEquals("text/html; charset=UTF-8", CharsetConverter.convertContentType("text/html;"));
        
        CharsetConverter.setEnabled(false);
        assertEquals("text/html; charset=Windows-31J",
        CharsetConverter.convertContentType("text/html; charset=Windows-31J"));
        assertEquals("text/html;charset=windows-31j",
        CharsetConverter.convertContentType("text/html;charset=windows-31j"));
        assertEquals("text/html; charset=UTF-8", CharsetConverter.convertContentType("text/html"));
        assertEquals("text/html; charset=UTF-8", CharsetConverter.convertContentType("text/html;"));
    }
    
    /**
    * Test method for
    * {@link org.seasar.mayaa.impl.engine.CharsetConverter#extractEncoding(java.lang.String)}.
    */
    @Test
    public void testExtractEncoding() {
        CharsetConverter.setEnabled(true);
        assertEquals("Windows-31J", CharsetConverter.extractEncoding("text/html; charset=Shift_JIS"));
        assertEquals("Windows-31J", CharsetConverter.extractEncoding("text/html;charset=shift_jis"));
        assertEquals("UTF-8", CharsetConverter.extractEncoding("text/html"));
        assertEquals("UTF-8", CharsetConverter.extractEncoding("text/html;"));
        
        CharsetConverter.setEnabled(false);
        assertEquals("Windows-31J", CharsetConverter.extractEncoding("text/html; charset=Windows-31J"));
        assertEquals("windows-31j", CharsetConverter.extractEncoding("text/html;charset=windows-31j"));
        assertEquals("UTF-8", CharsetConverter.extractEncoding("text/html"));
        assertEquals("UTF-8", CharsetConverter.extractEncoding("text/html;"));
        assertEquals("UTF-8", CharsetConverter.extractEncoding(""));
        assertEquals("UTF-8", CharsetConverter.extractEncoding(null));
    }
    
}

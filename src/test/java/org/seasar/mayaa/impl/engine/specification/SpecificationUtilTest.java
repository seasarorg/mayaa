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
package org.seasar.mayaa.impl.engine.specification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.seasar.mayaa.engine.specification.QName;

public class SpecificationUtilTest {

    /*
     * Test method for 'org.seasar.mayaa.impl.engine.specification.SpecificationUtil.parseQName(String)'
     */
    @Test
    public void testParseQName() {
        QName qName1 = SpecificationUtil.parseQName("{http://mayaa.seasar.org}id");
        assertEquals("http://mayaa.seasar.org", qName1.getNamespaceURI().getValue());
        assertEquals("id", qName1.getLocalName());

        QName qName2 = SpecificationUtil.parseQName("{ http://mayaa.seasar.org } id ");
        assertEquals("http://mayaa.seasar.org", qName2.getNamespaceURI().getValue());
        assertEquals("id", qName2.getLocalName());

        try {
            SpecificationUtil.parseQName("");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("{foobar");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("{foobar", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("{foo}  ");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("{foo}  ", e.getMessage());
        }

        try {
            SpecificationUtil.parseQName("bar}id");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("bar}id", e.getMessage());
        }
    }

}

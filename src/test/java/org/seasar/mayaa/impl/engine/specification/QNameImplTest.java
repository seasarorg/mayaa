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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.engine.specification.QName;
import org.seasar.mayaa.impl.CONST_IMPL;

public class QNameImplTest {
  @Test
  public void testGetInstanceWithDefaultNamespaceURI() {
    QName qn = QNameImpl.getInstance("localName");

    assertEquals(CONST_IMPL.URI_MAYAA, qn.getNamespaceURI());
    assertEquals("localName", qn.getLocalName());
  }

  @Test
  public void testGetInstance2() {
    QName qn = QNameImpl.getInstance("", "div");

    assertEquals(URIImpl.NULL_NS_URI, qn.getNamespaceURI());
    assertEquals("div", qn.getLocalName());
  }

  @Test
  public void testGetInstance3() {
    QName qn = QNameImpl.getInstance("http://www.w3.org/TR/html4", "div");

    assertEquals(CONST_IMPL.URI_HTML, qn.getNamespaceURI());
    assertEquals("div", qn.getLocalName());
  }

  @Test
  public void testGetSingleInstance() {
    QName qn1 = QNameImpl.getInstance("http://www.w3.org/TR/html4", "div");
    QName qn2 = QNameImpl.getInstance("http://www.w3.org/TR/html4", "div");

    assertEquals(CONST_IMPL.URI_HTML, qn1.getNamespaceURI());
    assertEquals("div", qn1.getLocalName());

    // 同じインスタンスを返す
    assertTrue(qn1 == qn2);
    assertEquals(qn1, qn2);
  }

}

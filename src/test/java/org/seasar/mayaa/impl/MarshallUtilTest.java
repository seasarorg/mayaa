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
package org.seasar.mayaa.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.seasar.mayaa.PositionAware;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MarshallUtilTest {

    @Test
    public void testSetPosition() {
        PositionAware posAware = new ParameterAwareImpl();
        MarshallUtil.setPosition(posAware, "/testID", 5);
        assertEquals("/testID", posAware.getSystemID());
        assertEquals(5, posAware.getLineNumber());
    }

    @Test
    public void testMarshall() {
        MarshallTest before = new MarshallTestImpl1();
        Object result = MarshallUtil.marshall(MarshallTestImpl2.class,
                MarshallTest.class, before, "/testID", 5);
        assertTrue(result instanceof MarshallTestImpl2);
        MarshallTest current = (MarshallTest)result;
        assertEquals("MarshallTestImpl1", current.getName1());
        assertEquals("MarshallTestImpl2", current.getName2());
        assertEquals("/testID", current.getSystemID());
        assertEquals(5, current.getLineNumber());
    }

    public static interface MarshallTest extends PositionAware {

        String getName1();

        String getName2();

    }

    public static class MarshallTestImpl1
            extends ParameterAwareImpl implements MarshallTest {

        private static final long serialVersionUID = 1L;

        public String getName1() {
            return "MarshallTestImpl1";
        }

        public String getName2() {
            return "MarshallTestImpl1";
        }

    }

    public static class MarshallTestImpl2
            extends ParameterAwareImpl implements MarshallTest {

        private static final long serialVersionUID = 1L;

        private MarshallTest _before;

        public MarshallTestImpl2(MarshallTest before) {
            _before = before;
        }

        public String getName1() {
            return _before.getName1();
        }

        public String getName2() {
            return "MarshallTestImpl2";
        }

    }

}

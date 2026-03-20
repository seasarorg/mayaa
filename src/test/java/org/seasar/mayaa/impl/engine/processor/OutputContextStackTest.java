/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.seasar.mayaa.test.util.ManualProviderFactory;

public class OutputContextStackTest {

    @BeforeEach
    public void setUp() {
        ManualProviderFactory.setUp(this);
    }

    @AfterEach
    public void tearDown() {
        ManualProviderFactory.tearDown();
    }

    @Test
    public void testPushPopCurrent() {
        assertEquals(OutputContext.HTML_BODY, OutputContextStack.current());

        OutputContextStack.push(OutputContext.SCRIPT);
        assertEquals(OutputContext.SCRIPT, OutputContextStack.current());

        OutputContextStack.push(OutputContext.STYLE);
        assertEquals(OutputContext.STYLE, OutputContextStack.current());

        OutputContextStack.pop();
        assertEquals(OutputContext.SCRIPT, OutputContextStack.current());

        OutputContextStack.pop();
        assertEquals(OutputContext.HTML_BODY, OutputContextStack.current());
    }

    @Test
    public void testPopWhenEmpty() {
        assertEquals(OutputContext.HTML_BODY, OutputContextStack.pop());
        assertEquals(OutputContext.HTML_BODY, OutputContextStack.current());
    }
}

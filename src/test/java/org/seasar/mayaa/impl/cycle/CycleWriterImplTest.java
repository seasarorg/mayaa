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
package org.seasar.mayaa.impl.cycle;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CycleWriterImplTest {

    @Test
    public void testGetBuffer() throws IOException {
        try (CycleWriterImpl writer = new CycleWriterImpl(null)) {
            writer.write("test string");
            assertEquals("test string", writer.getString());
        }
    }

    @Test
    public void testClearBuffer() throws IOException {
        try (CycleWriterImpl writer = new CycleWriterImpl(null)) {
            writer.write("test string");
            writer.clearBuffer();
            assertEquals("", writer.getString());
        }
    }

}

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
package org.seasar.mayaa.impl.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class FileSourceDescriptorTest {

    /*
     * Test method for 'org.seasar.mayaa.impl.source.FileSourceDescriptor.exists()'
     */
    @Test
    public void testExists() {
        FileSourceDescriptor fd = new FileSourceDescriptor();
        String home = System.getProperty("java.home");
        fd.setRoot(home);
        fd.setSystemID("bin/java");

        assertTrue(fd.exists());
        assertEquals("java", fd.getFile().getName());
    }

}

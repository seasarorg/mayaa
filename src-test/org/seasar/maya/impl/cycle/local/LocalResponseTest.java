/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
 *
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which
 * accompanies this distribution, and is available at
 *
 *     http://www.seasar.org/SEASAR-LICENSE.TXT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.seasar.maya.impl.cycle.local;

import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.TestCase;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class LocalResponseTest extends TestCase {

    public LocalResponseTest(String name) {
        super(name);
    }
    
    public void testWriteToUnderlyingObject() {
        LocalResponse response = new LocalResponse();
        response.writeToUnderlyingObject("<html></html>");
        assertEquals("<html></html>", new String(response.getResult()));
    }

    public void testGetOutputStream() throws IOException {
        LocalResponse response = new LocalResponse();
        PrintWriter writer = new PrintWriter(response.getUnderlyingOutputStream());
        writer.write("<html></html>");
        writer.flush();
        assertEquals("<html></html>", new String(response.getResult()));
    }
 
    public void testFlush() {
        LocalResponse response = new LocalResponse();
        response.write("<html></html>");
        assertEquals("", new String(response.getResult()));
        response.flush();
        assertEquals("<html></html>", new String(response.getResult()));
    }
    
}

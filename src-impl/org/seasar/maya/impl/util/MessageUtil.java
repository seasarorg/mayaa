/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.seasar.maya.impl.source.JavaSourceDescriptor;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class MessageUtil {

    private MessageUtil() {
    }
    
    private static Map _propFiles = new HashMap();
    
    public static String getMessage(Class clazz, int index) {
        Package key = clazz.getPackage();
        Properties properties =  (Properties)_propFiles.get(key);
        if(properties == null) {
            SourceDescriptor source = new JavaSourceDescriptor("message.properties", clazz);
            properties = new Properties();
            _propFiles.put(key, properties);
            if(source.exists()) {
	            try {
	                properties.load(source.getInputStream());
	            } catch (IOException e) {
	                throw new RuntimeException(e);
	            }
            }
        }
        StringBuffer propertyName = new StringBuffer(clazz.getName());
        if(index > 0) {
            propertyName.append(".").append(index); 
        }
        String message = properties.getProperty(propertyName.toString());
        if(StringUtil.isEmpty(message)) {
        	message = "!" + clazz.getName() +  "!";
        }
        return message;
    }
    
}

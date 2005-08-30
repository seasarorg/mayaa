/*
 * Copyright (c) 2004-2005 the Seasar Foundation and the Others.
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

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cyberneko.html.HTMLEntities;
import org.seasar.maya.impl.source.ClassLoaderSourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public final class StringUtil {

    private static Map _propFiles = new HashMap();
	
    private StringUtil() {
    }

    public static boolean isEmpty(String test) {
        return test == null || test.length() == 0;
    }

    public static boolean hasValue(String test) {
        return !isEmpty(test);
    }
    
    public static String preparePath(String path) {
        if(path == null) {
            return "";
        }
        path = path.trim();
        path = path.replace(File.separatorChar, '/');
        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if(path.length() > 0 && !path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public static String resolveEntity(String blockString) {
        StringBuffer buffer = new StringBuffer();
        int start = blockString.indexOf("&");
        if(start == -1) {
            return blockString;
        }
        buffer.append(blockString.substring(0, start));
        String entity;
        while(true) {
            int end = blockString.indexOf(";", start);
            if(end == -1) {
                buffer.append(blockString.substring(start));
                break;
            }
            entity = blockString.substring(start + 1, end);
            int value = HTMLEntities.get(entity);
            if(value != -1) {
                buffer.append((char)value);
            } else {
                buffer.append(blockString.substring(start, end + 1));
            }
            start = blockString.indexOf("&", end);
            if(start == -1) {
                buffer.append(blockString.substring(end + 1));
                break;
            }
            if(start != end + 1) {
                buffer.append(blockString.substring(end + 1, start));
            }
            if(start == blockString.length()) {
                break;
            }
        }
        return buffer.toString();
    }
    
    public static String[] parsePath(String path, String suffixSeparator) {
        String[] ret = new String[3];
        int paramOffset = path.indexOf('?');
        if(paramOffset >= 0) {
            path = path.substring(0, paramOffset);
        }
        int lastSlashOffset = path.lastIndexOf('/');
        String folder =  "";
        String file = path;
        if(lastSlashOffset >= 0) {
            folder = path.substring(0, lastSlashOffset + 1);
            file = path.substring(lastSlashOffset + 1);
        }
        int lastDotOffset = file.lastIndexOf('.');
        if(lastDotOffset > 0) {
            ret[2] = file.substring(lastDotOffset + 1);
            file = file.substring(0, lastDotOffset);
        } else {
            ret[2] = "";
        }
        int suffixSeparatorOffset = file.lastIndexOf(suffixSeparator);
        if(suffixSeparatorOffset > 0) {
            ret[0] = folder + file.substring(0, suffixSeparatorOffset);
            ret[1] = file.substring(suffixSeparatorOffset + suffixSeparator.length());
        } else {
            ret[0] = folder + file;
            ret[1] = "";
        }
        return ret;
    }
    
    public static String getMessage(Class clazz, int index, String[] params) {
        Package key = clazz.getPackage();
        Properties properties =  (Properties)_propFiles.get(key);
        if(properties == null) {
            ClassLoaderSourceDescriptor source = new ClassLoaderSourceDescriptor();
            source.setSystemID("message.properties");
            source.setNeighborClass(clazz);
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
        if(isEmpty(message)) {
        	message = "!" + clazz.getName() +  "!";
        }
        if(params == null) {
            params = new String[0];
        }
        return MessageFormat.format(message, params);
    }
    
}

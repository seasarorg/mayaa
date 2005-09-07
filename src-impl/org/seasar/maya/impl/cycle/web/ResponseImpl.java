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
package org.seasar.maya.impl.cycle.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.seasar.maya.impl.cycle.AbstractResponse;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ResponseImpl extends AbstractResponse {

    private static final long serialVersionUID = -4653384671998376182L;

    private HttpServletResponse _httpServletResponse;

    public ResponseImpl(HttpServletResponse httpServletResponse) {
        if(httpServletResponse == null) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse = httpServletResponse;
    }
    
    public Object getUnderlyingObject() {
        return _httpServletResponse;
    }

    public void addHeader(String name, String value) {
        if(StringUtil.isEmpty(name)) {
            return;
        }
        _httpServletResponse.addHeader(name, value);
    }

    public void setStatus(int code) {
        _httpServletResponse.setStatus(code);
    }

    protected void setContentTypeToUnderlyingObject(String contentType) {
        if(StringUtil.isEmpty(contentType)) {
            throw new IllegalArgumentException();
        }
        _httpServletResponse.setContentType(contentType);
    }

    public OutputStream getOutputStream() {
        try {
            return _httpServletResponse.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String encodeURL(String url) {
        if(StringUtil.isEmpty(url)) {
            throw new IllegalArgumentException();
        }
        return _httpServletResponse.encodeURL(url);
    }

    protected void redirect(String url) {
    	try {
    		_httpServletResponse.sendRedirect(url);
    	} catch(IOException e) {
    		throw new RuntimeException(e);
    	}
    }
    
}

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
package org.seasar.maya.impl.engine;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.seasar.maya.engine.Engine;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.error.ErrorHandler;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.engine.error.JspContextErrorHandler;
import org.seasar.maya.impl.engine.specification.SpecificationImpl;
import org.seasar.maya.impl.provider.EngineSettingImpl;
import org.seasar.maya.impl.util.EngineUtil;
import org.seasar.maya.impl.util.ExpressionUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.EngineSetting;
import org.seasar.maya.provider.PageContextSetting;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * Engineの実装クラス。ファクトリを通じて生成される。
 * 生成後、デフォルト設定XMLのSourceDescriptorをセットする。 
 * @author Masataka Kurihara (Gluegent, Inc)
 */
public class EngineImpl extends SpecificationImpl implements Engine, CONST_IMPL {
    
	private DummyServlet _dummyServlet;
    private EngineSetting _engineSetting;
    private ErrorHandler _errorHandler;
    private String _welcomeFileName;
    
    /**
     * ファクトリにて呼び出される。
     */
    public EngineImpl() {
        super(QM_ENGINE, null);
    }

    public void setEngineSetting(EngineSetting engineSetting) {
        _engineSetting = engineSetting;
    }
    
    public EngineSetting getEngineSetting() {
        if(_engineSetting == null) {
            _engineSetting = new EngineSettingImpl();
        }
        return _engineSetting;
    }
    
    public void setErrorHandler(ErrorHandler errorHandler) {
        _errorHandler = errorHandler;
    }

    public ErrorHandler getErrorHandler() {
	    if(_errorHandler == null) {
	        _errorHandler = new JspContextErrorHandler();
	    }
        return _errorHandler;
    }
    
	public void putParameter(String name, String value) {
	    if(StringUtil.isEmpty(name) || StringUtil.isEmpty(value)) {
	        throw new IllegalArgumentException();
	    }
        if("welcomeFileName".equalsIgnoreCase(name)) {
            _welcomeFileName = value;
            return;
        }
        // TODO 適切な例外型への変更（対応していないカスタマイズ内容） 
        throw new IllegalStateException();
    }
    
    public String getKey() {
        return "/engine";
    }
    
    /**
     * リクエストされたパスを分解して配列で返す。配列の内容は、
     * [0]=ページ名
     * [1]=ユーザーが強制指定する接尾辞
     * [2]=ページ拡張子
     * @param path HttpServletRequest#getPathInfo()の値
     * @return 情報を分解して格納したStringの配列。
     */
    protected String[] getRequestedPageInfo(EngineSetting setting, String path) {
        String[] requestedPageInfo = new String[3];
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
            // 先頭にドットがある場合は、このブロックで処理しない
            requestedPageInfo[2] = file.substring(lastDotOffset + 1);
            file = file.substring(0, lastDotOffset);
        } else {
            requestedPageInfo[2] = "";
        }
        String suffixSeparator = setting.getSuffixSeparator();
        int suffixSeparatorOffset = file.lastIndexOf(suffixSeparator);
        if(suffixSeparatorOffset > 0) {
            // 先頭にセパレータがある場合は、このブロックで処理しない
            requestedPageInfo[0] = folder + file.substring(0, suffixSeparatorOffset);
            requestedPageInfo[1] = file.substring(suffixSeparatorOffset + suffixSeparator.length());
        } else {
            requestedPageInfo[0] = folder + file;
            requestedPageInfo[1] = "";
        }
        return requestedPageInfo;
    }
    
    public synchronized Page getPage(String pageName, String extension) {
        String key = EngineUtil.createPageKey(pageName, extension);
        Page page = EngineUtil.getPage(this, key);
        if(page == null) {
            String path = PREFIX_PAGE + pageName + ".maya";
            ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
            SourceFactory factory = provider.getSourceFactory();
            SourceDescriptor source = factory.createSourceDescriptor(path);
            page = new PageImpl(this, pageName, extension);
            page.setSource(source);
            addChildSpecification(page);
        }
        return page;
    }

    private Throwable removeWrapperRuntimeException(Throwable t) {
        Throwable throwable = t ;
        while(throwable.getClass().equals(RuntimeException.class)) {
            if( throwable.getCause() == null ) {
                break;
            }
            throwable = throwable.getCause();
        }
        return throwable ;
    }
   
	public void doService(PageContext context, String pageName, 
			String requestedSuffix, String extension) throws IOException {
        if(context == null || StringUtil.isEmpty(pageName)) {
            throw new IllegalArgumentException();
        }
        ExpressionUtil.execEvent(this, QM_BEFORE_RENDER, context);
        Page page = getPage(pageName, extension);
        page.doPageRender(context, requestedSuffix);
        ExpressionUtil.execEvent(this, QM_AFTER_RENDER, context);
        context.getOut().flush();
	}
    
	private void prepareRequest(HttpServletResponse httpResponse) {
        httpResponse.addHeader("Pragma", "no-cache");
        httpResponse.addHeader("Cache-Control", "no-cache");
        httpResponse.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
	}
	
    public String getWelcomeFileName() {
        if(StringUtil.hasValue(_welcomeFileName)) {
            return _welcomeFileName;
        }
        return "index.html";
    }
    
	private String getRequestPath(HttpServletRequest httpRequest) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(StringUtil.preparePath(httpRequest.getServletPath()));
        String pathInfo = httpRequest.getPathInfo();
        buffer.append(StringUtil.preparePath(pathInfo));
        if(pathInfo.endsWith("/")) {
            buffer.append(StringUtil.preparePath(getWelcomeFileName()));
        }
        return buffer.toString();
	}

    private PageContext getPageContext(
    		ServletRequest request, ServletResponse response) {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
	    JspFactory factory = JspFactory.getDefaultFactory();
        if(_dummyServlet == null) {
        	_dummyServlet = new DummyServlet();
        }
		PageContextSetting pc = getEngineSetting().getPageContextSetting();
        PageContext context = factory.getPageContext(_dummyServlet, request, response, 
        		pc.getErrorPageURL(), pc.isNeedSession(), pc.getBufferSize(), pc.isAutoFlush());
        EngineUtil.setEngine(context, this);
        return context;
    }
    
    private void releasePageContext(PageContext context) {
    	if(context == null) {
    		throw new IllegalArgumentException();
    	}
	    JspFactory factory = JspFactory.getDefaultFactory();
	    factory.releasePageContext(context);
    }
 	
	private void handleError(ServletRequest request, ServletResponse response, Throwable t) {
    	PageContext context = getPageContext(request, response);
        try {
            t = removeWrapperRuntimeException(t);
            getErrorHandler().doErrorHandle(context, t);
        } catch(Throwable tx) {
            if(tx instanceof RuntimeException) {
                throw (RuntimeException)tx;
            }
            throw new RuntimeException(tx);
    	} finally {
    		releasePageContext(context);
    	}
	}
	
    public void doService(ServletRequest request, ServletResponse response) 
    		throws IOException {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        if(response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse)response;
            prepareRequest(httpResponse);
        }
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
            EngineSetting setting = getEngineSetting();
            String[] requestedPageInfo = 
                getRequestedPageInfo(setting, getRequestPath(httpRequest));
            try {
                PageContext context = getPageContext(request, response);
	            try {
	            	doService(context, requestedPageInfo[0], 
	            	        requestedPageInfo[1], requestedPageInfo[2]);
		        } finally {
		            releasePageContext(context);
		        }
            } catch(IOException e) {
            	throw e;
            } catch(Throwable t) {
            	handleError(request, response, t);
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
	public void doResourceService(ServletRequest request, ServletResponse response)
			throws IOException {
        if(request == null || response == null) {
            throw new IllegalArgumentException();
        }
        if(request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest)request;
			StringBuffer buffer = new StringBuffer();
			buffer.append(StringUtil.preparePath(httpRequest.getServletPath()));
			buffer.append(StringUtil.preparePath(httpRequest.getPathInfo()));
			String path = PREFIX_PAGE + buffer.toString();
			ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
	        SourceFactory factory = provider.getSourceFactory();
			SourceDescriptor source = factory.createSourceDescriptor(path);
			InputStream stream = source.getInputStream();
			if(stream != null) {
				Writer out = response.getWriter();
				for(int i = stream.read(); i != -1; i = stream.read()) {
					out.write(i);
				}
				out.flush();
			} else {
		        if(response instanceof HttpServletResponse) {
		            HttpServletResponse httpResponse = (HttpServletResponse)response;
		            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		        }
			}
        } else {
            throw new IllegalStateException();
        }
	}

}

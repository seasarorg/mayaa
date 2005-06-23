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
package org.seasar.maya.component.engine.processor;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.seasar.maya.component.CONST_COMPONENT;
import org.seasar.maya.component.util.ComponentUtil;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.engine.PageImpl;
import org.seasar.maya.impl.engine.processor.AbstractAttributableProcessor;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.provider.ServiceProvider;
import org.seasar.maya.provider.factory.ServiceProviderFactory;
import org.seasar.maya.source.SourceDescriptor;
import org.seasar.maya.source.factory.SourceFactory;

/**
 * テンプレートへのページ埋め込み機能を実現するプロセッサ。「startComponent」および
 * 「endComponent」と連動する。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComponentPageProcessor extends AbstractAttributableProcessor
		implements CONST_COMPONENT {

    private String _path;
    private String _namespaceURI;
    private Page _page;
    
    // MLD property
    public void setPath(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        _path = path;
    }
    
    // MLD property
    public void setNamespaceURI(String namespaceURI) {
        if(StringUtil.isEmpty(namespaceURI)) {
            throw new IllegalArgumentException();
        }
        _namespaceURI = namespaceURI;
    }
    
    public String getInformalAttrituteURI() {
        if(StringUtil.isEmpty(_namespaceURI)) {
            return URI_COMPONENT;
        }
        return _namespaceURI;
    }
    
    /**
     * パス指定されたコンポーネントページを生成する。
     * @return コンポーネントページ。
     */
    protected Page preparePage() {
        if(StringUtil.isEmpty(_path)) {
            throw new IllegalStateException();
        }
        String[] pagePath = ComponentUtil.parsePath(_path);
        Page page =  new PageImpl(getTemplate(), pagePath[0], pagePath[1]);
        String sourcePath = PREFIX_PAGE + pagePath[0] + ".maya";
        ServiceProvider provider = ServiceProviderFactory.getServiceProvider();
        SourceFactory factory = provider.getSourceFactory();
        SourceDescriptor source = factory.createSourceDescriptor(sourcePath);
        if(source.exists()) {
            page.setSource(source);
        }
        return page;
    }
    
    /**
     * 「p:startComponent」を再帰で探す。
     * @param processor 再帰で渡されるプロセッサ。
     * @return 見つけたstartComponentもしくはnull。
     */
    protected StartComponentProcessor findStart(TemplateProcessor processor) {
        for(int i = 0; i < processor.getChildProcessorSize(); i++) {
            TemplateProcessor childProcessor = processor.getChildProcessor(i);
            if(childProcessor instanceof StartComponentProcessor) {
                return (StartComponentProcessor)childProcessor;
            }
            StartComponentProcessor doContentsProcessor = findStart(childProcessor);
            if(doContentsProcessor != null) {
                return doContentsProcessor;
            }
        }
        return null;
    }
    
    /**
     * 「p:endComponent」の内側の描画。
     * @param context カレントのコンテキスト。
     * @return Template#doTemplateRender()の戻り値。
     */
    public int renderChildren(PageContext context) {
        Template template = getTemplate();
        for(int i = 0; i < getChildProcessorSize(); i++) {
            TemplateProcessor processor = getChildProcessor(i);
            if(template.doTemplateRender(context, processor) == Tag.SKIP_PAGE) {
                return Tag.SKIP_PAGE;
            }
        }
        return Tag.EVAL_PAGE;
    }
    
	protected int writeStartElement(PageContext context) {
        synchronized(this) {
            if(_page == null) {
                _page = preparePage();
            }
        }
        Template template = _page.getTemplate(context, "");
        template.setParentProcessor(this, 0);
        StartComponentProcessor start = findStart(template);
        if(start != null) {
            template.doTemplateRender(context, start);
            return Tag.SKIP_BODY;
        }
        // TODO 適切な例外型に変更（p:startComponentが無かったとき）
        throw new IllegalStateException();
    }
    
	protected void writeEndElement(PageContext context) {
	}

}

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
package org.seasar.maya.impl.engine.processor;

import org.seasar.maya.cycle.ServiceCycle;
import org.seasar.maya.engine.Page;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.processor.TemplateProcessor;
import org.seasar.maya.impl.CONST_IMPL;
import org.seasar.maya.impl.builder.PageNotFoundException;
import org.seasar.maya.impl.engine.PageImpl;
import org.seasar.maya.impl.source.PageSourceDescriptor;
import org.seasar.maya.impl.util.CycleUtil;
import org.seasar.maya.impl.util.StringUtil;
import org.seasar.maya.source.SourceDescriptor;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ComponentPageProcessor extends AbstractAttributableProcessor
		implements CONST_IMPL {

	private static final long serialVersionUID = -1240398725406503403L;
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
            return URI_MAYA;
        }
        return _namespaceURI;
    }
    
    // TODO WebRequestのパス処理機能との共有を考える。
    private String[] parsePath(String path) {
        if(StringUtil.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        String[] pagePath = new String[2];
        int dotPos = path.lastIndexOf(".");
        if(dotPos > 0 && dotPos < path.length() - 1) {
            pagePath[0] = StringUtil.preparePath(path.substring(0, dotPos));
            pagePath[1] = path.substring(dotPos + 1);
        } else {
            pagePath[0] = StringUtil.preparePath(path);
            pagePath[1] = "";
        }
        return pagePath;
    }

    protected Page preparePage() {
        if(StringUtil.isEmpty(_path)) {
            throw new IllegalStateException();
        }
        String[] pagePath = parsePath(_path);
        Page page =  new PageImpl(getTemplate(), pagePath[0], pagePath[1]);
        String sourcePath = pagePath[0] + ".maya";
        SourceDescriptor source = new PageSourceDescriptor(sourcePath);
        if(source.exists()) {
            page.setSource(source);
        }
        return page;
    }
    
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
    
    public ProcessStatus renderChildren() {
        Template template = getTemplate();
        for(int i = 0; i < getChildProcessorSize(); i++) {
            TemplateProcessor processor = getChildProcessor(i);
            if(template.doTemplateRender(processor) == SKIP_PAGE) {
                return SKIP_PAGE;
            }
        }
        return EVAL_PAGE;
    }
    
	protected ProcessStatus writeStartElement() {
        synchronized(this) {
            if(_page == null) {
                _page = preparePage();
            }
        }
        ServiceCycle cycle = CycleUtil.getServiceCycle();
        Template template = _page.getTemplate(cycle.getRequest().getRequestedSuffix());
        if(template == null) {
            throw new PageNotFoundException(_page.getKey());
        }
        template.setParentProcessor(this, 0);
        StartComponentProcessor start = findStart(template);
        if(start != null) {
            template.doTemplateRender(start);
            return SKIP_BODY;
        }
        throw new StartComponentNotFoundException(template);
    }
    
	protected void writeEndElement() {
	}

}

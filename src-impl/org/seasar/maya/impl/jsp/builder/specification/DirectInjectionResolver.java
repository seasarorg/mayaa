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
package org.seasar.maya.impl.jsp.builder.specification;

import org.seasar.maya.builder.library.LibraryManager;
import org.seasar.maya.builder.specification.InjectionChain;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.QName;
import org.seasar.maya.engine.specification.SpecificationNode;
import org.seasar.maya.impl.util.SpecificationUtil;
import org.seasar.maya.provider.factory.ServiceProviderFactory;

/**
 * テンプレート中に、JSP風に直接プロセッサQNameを記述した場合の解決を行うレゾルバ。
 * <pre>
 * &lt;html xmlns:c="http://maya.seasar.org/jstl/core"&gt;
 *   &lt;body&gt;
 *     &lt;p&gt;&lt;c:out c:value="${ greeting }"&gt;&lt;/p&gt;
 *   &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * 上記の「c:out」エレメントのような場合。
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class DirectInjectionResolver implements InjectionResolver {

    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

    public SpecificationNode getNode(Template template,
            SpecificationNode original, InjectionChain chain) {
        QName qName = original.getQName();
        LibraryManager libraryManager = 
            ServiceProviderFactory.getServiceProvider().getTemplateBuilder().getLibraryManager();
        if(libraryManager.getProcessorDefinition(qName) != null) {
            return SpecificationUtil.createInjectedNode(
                    qName, qName.getNamespaceURI(), original);
        }
        return chain.getNode(template, original);
    }

}

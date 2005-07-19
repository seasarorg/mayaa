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
package org.seasar.maya.impl.provider.factory;

import org.seasar.maya.impl.cycle.el.AbstractExpressionFactory;
import org.seasar.maya.impl.cycle.el.ognl2.Ognl2ExpressionFactory;
import org.seasar.maya.provider.Parameterizable;
import org.xml.sax.Attributes;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ExpressionTagHandler extends AbstractParameterizableTagHandler {
    
    private ServiceTagHandler _parent;
    private AbstractExpressionFactory _expressionFactory;
    
    public ExpressionTagHandler(ServiceTagHandler parent) {
        if(parent == null) {
            throw new IllegalArgumentException();
        }
        _parent = parent;
        putHandler("resolver", new ExpressionResolverTagHandler(this));
    }
    
    protected void start(Attributes attributes) {
        _expressionFactory = new Ognl2ExpressionFactory();
        _parent.getServiceProvider().setExpressionFactory(_expressionFactory);
    }
    
    protected void end(String body) {
        _expressionFactory = null;
    }
    
    public AbstractExpressionFactory getExpressionFactory() {
        if(_expressionFactory == null) {
            throw new IllegalStateException();
        }
        return _expressionFactory;
    }
    
    public Parameterizable getParameterizable() {
        return getExpressionFactory();
    }

}

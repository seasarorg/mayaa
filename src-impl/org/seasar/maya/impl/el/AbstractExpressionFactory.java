/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
 * 
 * Licensed under the Seasar Software License, v1.1 (aka "the License");
 * you may not use this file except in compliance with the License which 
 * accompanies this distribution, and is available at
 * 
 *     http://homepage3.nifty.com/seasar/SEASAR-LICENSE.TXT
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.seasar.maya.impl.el;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.seasar.maya.el.CompiledExpression;
import org.seasar.maya.el.ExpressionFactory;
import org.seasar.maya.el.resolver.ExpressionResolver;
import org.seasar.maya.impl.el.resolver.CompositeExpressionResolver;
import org.seasar.maya.impl.el.resolver.ImplicitObjectExpressionResolver;
import org.seasar.maya.impl.el.resolver.ScopedAttributeExpressionResolver;
import org.seasar.maya.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractExpressionFactory implements ExpressionFactory {
    
    private String _blockStart = ExpressionBlockIterator.BLOCK_START_JSP;
    private String _blockEnd = ExpressionBlockIterator.BLOCK_END_JSP;
    private CompositeExpressionResolver _expressionResolver;
    private CompositeExpressionResolver _userExpressionResolver;

    public AbstractExpressionFactory() {
        prepareExpressionResolver();
    }
    
    protected void prepareExpressionResolver() {
        _userExpressionResolver = new CompositeExpressionResolver();
        _expressionResolver = new CompositeExpressionResolver();
        _expressionResolver.add(new ImplicitObjectExpressionResolver());
        _expressionResolver.add(_userExpressionResolver);
        _expressionResolver.add(new ScopedAttributeExpressionResolver());
    }

    public void addExpressionResolver(ExpressionResolver resolver) {
    	if(resolver == null) {
    		throw new IllegalArgumentException();
    	}
    	_userExpressionResolver.add(resolver);
     }
    
    public void setBlockStart(String blockStart) {
        if(StringUtil.isEmpty(blockStart)) {
            throw new IllegalArgumentException();
        }
        _blockStart = blockStart;
    }
    
    public void setBlockEnd(String blockEnd) {
        if(StringUtil.isEmpty(blockEnd)) {
            throw new IllegalArgumentException();
        }
        _blockEnd = blockEnd;
    }
    
    public ExpressionResolver getExpressionResolver() {
    	return _expressionResolver;
    }
    
    protected abstract CompiledExpression compile(
            ExpressionBlock expressionBlock, Class expectedType);
    
    public CompiledExpression createExpression(String expression, Class expectedType) {
        if(StringUtil.isEmpty(expression) || expectedType == null) {
            throw new IllegalArgumentException();
        }
        List list = new ArrayList();
        for(Iterator it = new ExpressionBlockIterator(expression, _blockStart, _blockEnd);
        	it.hasNext();) {
            ExpressionBlock block = (ExpressionBlock)it.next();
                list.add(compile(block, expectedType));
        }
        if(list.size() == 0) {
    	    throw new SyntaxException(expression);
        } else if(list.size() == 1) {
    	    return (CompiledExpression)list.get(0);
    	}
	    CompiledExpression[] compiled = 
	        (CompiledExpression[])list.toArray(new CompiledExpression[list.size()]);
	    return new ComplexExpression(expression, expectedType, compiled);
    }

}

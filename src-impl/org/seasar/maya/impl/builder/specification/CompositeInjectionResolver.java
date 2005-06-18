/*
 * Copyright (c) 2004-2005 the Seasar Project and the Others.
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
package org.seasar.maya.impl.builder.specification;

import java.util.ArrayList;
import java.util.List;

import org.seasar.maya.builder.specification.InjectionChain;
import org.seasar.maya.builder.specification.InjectionResolver;
import org.seasar.maya.engine.Template;
import org.seasar.maya.engine.specification.SpecificationNode;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class CompositeInjectionResolver implements InjectionResolver {
    
	private List _resolvers = new ArrayList();
	
    /**
     * ƒŒƒ]ƒ‹ƒo‚ð’Ç‰Á‚·‚éB
     * @param resolver ’Ç‰Á‚µ‚½‚¢ƒŒƒ]ƒ‹ƒoB
     */
    public void add(InjectionResolver resolver) {
        if(resolver == null) {
            throw new IllegalArgumentException();
        }
        synchronized (_resolvers) {
            _resolvers.add(resolver);
        }
    }
    
    public SpecificationNode getNode(
            Template template, SpecificationNode original, InjectionChain external) {
        if(template == null || original == null || external == null) {
            throw new IllegalArgumentException();
        }
        if(_resolvers.size() > 0) {
            InjectionChainImpl first = new InjectionChainImpl(external);
            return first.getNode(template, original);
        }
        return external.getNode(template, original);
    }

    private class InjectionChainImpl implements InjectionChain {
        
        private int _index;
        private InjectionChain _external;
        
        public InjectionChainImpl(InjectionChain external) {
            if (external == null) {
                throw new IllegalArgumentException();
            }
            _external = external;
        }
        
        public SpecificationNode getNode(Template template, SpecificationNode original) {
            if(template == null || original == null) {
                throw new IllegalArgumentException();
            }
            if(_index < _resolvers.size()) {
                InjectionResolver resolver = (InjectionResolver)_resolvers.get(_index);
                _index++;
                InjectionChain chain;
                if(_index == _resolvers.size()) {
                    chain = _external;
                } else {
                    chain = this;
                }
                return resolver.getNode(template, original, chain);
            }
            throw new IndexOutOfBoundsException();
        }

    }
    
    public void putParameter(String name, String value) {
        throw new UnsupportedOperationException();
    }

}

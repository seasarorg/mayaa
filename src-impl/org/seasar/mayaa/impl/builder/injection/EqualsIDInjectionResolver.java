/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.mayaa.impl.builder.injection;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.builder.injection.InjectionChain;
import org.seasar.mayaa.builder.injection.InjectionResolver;
import org.seasar.mayaa.engine.specification.CopyToFilter;
import org.seasar.mayaa.engine.specification.NodeAttribute;
import org.seasar.mayaa.engine.specification.NodeObject;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.engine.specification.SpecificationNode;
import org.seasar.mayaa.impl.CONST_IMPL;
import org.seasar.mayaa.impl.ParameterAwareImpl;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class EqualsIDInjectionResolver extends ParameterAwareImpl
        implements InjectionResolver, CONST_IMPL {

    private static final Log LOG =
        LogFactory.getLog(EqualsIDInjectionResolver.class);

    private CopyToFilter _idFilter = new CheckIDCopyToFilter();
    private boolean _reportResolvedID = true;
    private boolean _useHtmlID = true;
    private boolean _useXhtmlID = true;

    protected CopyToFilter getCopyToFilter() {
        return _idFilter;
    }

    protected boolean isReportResolvedID() {
        return _reportResolvedID;
    }

    protected String getID(SpecificationNode node) {
        if(node == null) {
            throw new IllegalArgumentException();
        }
        NodeAttribute attr = node.getAttribute(QM_ID);
        if(attr != null) {
            return attr.getValue();
        }
        if (_useXhtmlID) {
            attr = node.getAttribute(QX_ID);
            if(attr != null) {
                return attr.getValue();
            }
        }
        if (_useHtmlID) {
            attr = node.getAttribute(QH_ID);
            if(attr != null) {
                return attr.getValue();
            }
        }
        return null;
    }

    protected SpecificationNode getEqualsIDNode(
            SpecificationNode node, String id) {
        if(node == null || StringUtil.isEmpty(id)) {
            throw new IllegalArgumentException();
        }
        for(Iterator it = node.iterateChildNode(); it.hasNext(); ) {
            SpecificationNode child = (SpecificationNode)it.next();
            if(id.equals(SpecificationUtil.getAttributeValue(child, QM_ID))) {
                return child;
            }
            SpecificationNode ret = getEqualsIDNode(child, id);
            if(ret != null) {
                return ret;
            }
        }
        return null;
    }

    public SpecificationNode getNode(
            SpecificationNode original, InjectionChain chain) {
        if(original == null || chain == null) {
            throw new IllegalArgumentException();
        }
        String id = getID(original);
        if(StringUtil.hasValue(id)) {
            Specification spec = SpecificationUtil.findSpecification(original);
            SpecificationNode injected = null;
            while(spec != null) {
                SpecificationNode mayaa = SpecificationUtil.getMayaaNode(spec);
                if(mayaa != null) {
                    injected = getEqualsIDNode(mayaa, id);
                    if(injected != null) {
                        break;
                    }
                }
                spec = EngineUtil.getParentSpecification(spec);
            }
            if(injected != null) {
                if(QM_IGNORE.equals(injected.getQName())) {
                    return chain.getNode(original);
                }
                return injected.copyTo(getCopyToFilter());
            }
            if(isReportResolvedID()) {
                if(LOG.isWarnEnabled()) {
                    String systemID = original.getSystemID();
                    String lineNumber = Integer.toString(original.getLineNumber());
                    String msg = StringUtil.getMessage(
                            EqualsIDInjectionResolver.class, 0,
                            id, systemID, lineNumber);
                    LOG.warn(msg);
                }
            }
        }
        return chain.getNode(original);
    }

    // Parameterizable implements ------------------------------------

    public void setParameter(String name, String value) {
        if("reportUnresolvedID".equals(name)) {
            _reportResolvedID = ObjectUtil.booleanValue(value, true);
        }
        if ("useXhtmlID".equals(name)) {
            _useXhtmlID = ObjectUtil.booleanValue(value, true);
        }
        if ("useHtmlID".equals(name)) {
            _useHtmlID = ObjectUtil.booleanValue(value, true);
        }
        super.setParameter(name, value);
    }

    // support class ------------------------------------------------

    protected class CheckIDCopyToFilter implements CopyToFilter {

        public boolean accept(NodeObject test) {
            if(test instanceof NodeAttribute) {
                NodeAttribute attr = (NodeAttribute)test;
                return attr.getQName().equals(QM_ID) == false;
            }
            return true;
        }

    }

}

/*
 * Copyright 2004-2012 the Seasar Foundation and the Others.
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
package org.seasar.mayaa.impl.engine.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.seasar.mayaa.cycle.CycleWriter;
import org.seasar.mayaa.engine.Page;
import org.seasar.mayaa.engine.processor.ChildEvaluationProcessor;
import org.seasar.mayaa.engine.processor.InformalPropertyAcceptable;
import org.seasar.mayaa.engine.processor.ProcessStatus;
import org.seasar.mayaa.engine.processor.ProcessorProperty;
import org.seasar.mayaa.engine.specification.PrefixAwareName;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;
import org.seasar.mayaa.impl.util.collection.NullIterator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public abstract class AbstractAttributableProcessor
        extends TemplateProcessorSupport
        implements ChildEvaluationProcessor, InformalPropertyAcceptable {

    private static final long serialVersionUID = -1406205460425148574L;
    private boolean _childEvaluation;
    private List/*<Serializable(ProcessorProperty or PrefixAwareName)>*/
                    _attributes;

    private static final String PROCESS_TIME_INFO_KEY =
        AbstractAttributableProcessor.class.getName() + "#processTimeInfo";
    static {
        CycleUtil.registVariableFactory(PROCESS_TIME_INFO_KEY,
                new DefaultCycleLocalInstantiator() {
                    public Object create(Object owner, Object[] params) {
                        return new Stack/*<ProcesstimeInfo>*/();
                    }
                });
    }

    private void clearProcesstimeInfo() {
        CycleUtil.clearLocalVariable(PROCESS_TIME_INFO_KEY, this);
    }

    protected void prepareProcesstimeInfo() {
        Stack piStack = (Stack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null);
        if (piStack.size() == 0) {
            piStack.push(new ProcesstimeInfo());
        }
    }

    protected ProcesstimeInfo pushProcesstimeInfo() {
        Stack piStack = (Stack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null);
        return (ProcesstimeInfo) piStack.push(new ProcesstimeInfo());
    }

    protected ProcesstimeInfo peekProcesstimeInfo() {
        return (ProcesstimeInfo) ((Stack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null)).peek();
    }

    protected ProcesstimeInfo popProcesstimeInfo() {
        Stack piStack = (Stack) CycleUtil.getLocalVariable(
                PROCESS_TIME_INFO_KEY, this, null);
        return (ProcesstimeInfo) piStack.pop();
    }

    // MLD property
    public void setChildEvaluation(boolean childEvaluation) {
        _childEvaluation = childEvaluation;
    }

    // MLD method
    public void addInformalProperty(PrefixAwareName name, Serializable attr) {
        if (_attributes == null) {
            _attributes = new ArrayList();
        }
        _attributes.add(attr);
    }

    /**
     * @deprecated 1.1.9 代わりに{@link #getInformalPropertyClass()}を使う
     */
    public Class getPropertyClass() {
        return getInformalPropertyClass();
    }

    /**
     * @deprecated 1.1.9 代わりに{@link #getInformalPropertyClass()}を使う
     */
    public Class getExpectedClass() {
        return getInformalExpectedClass();
    }

    public Class getInformalPropertyClass() {
        return ProcessorProperty.class;
    }

    public Class getInformalExpectedClass() {
        return Object.class;
    }

    public Iterator iterateInformalProperties() {
        if (_attributes == null) {
            return NullIterator.getInstance();
        }
        return _attributes.iterator();
    }

    // processtime method
    public void addProcesstimeProperty(ProcessorProperty prop) {
        if (prop == null) {
            throw new IllegalArgumentException();
        }
        prepareProcesstimeInfo();
        ProcesstimeInfo info = peekProcesstimeInfo();
        info.addProcesstimeProperty(prop);
    }

    public boolean hasProcesstimeProperty(ProcessorProperty prop) {
        if (prop == null) {
            throw new IllegalArgumentException();
        }
        prepareProcesstimeInfo();
        ProcesstimeInfo info = peekProcesstimeInfo();
        return info.hasProcesstimeProperty(prop);
    }

    public Iterator iterateProcesstimeProperties() {
        prepareProcesstimeInfo();
        ProcesstimeInfo info = peekProcesstimeInfo();
        return info.iterateProcesstimeProperties();
    }

    protected abstract ProcessStatus writeStartElement();

    protected abstract void writeBody(String body);

    protected abstract void writeEndElement();

    /**
     * このメソッドをoverrideしないこと。
     * 代わりに{@link #processStart(Page)}をoverrideすること。
     */
    public ProcessStatus doStartProcess(Page topLevelPage) {
        if (_childEvaluation) {
            prepareProcesstimeInfo();
            pushProcesstimeInfo();
        }
        return processStart(topLevelPage);
    }

    /**
     * 子クラスはdoStartProcessではなくこのメソッドをoverrideすること。
     * ProcesstimeInfoを取得するには{@link #peekProcesstimeInfo()}を使用します。
     *
     * @param topLevelPage 描画トップレベルのページ。
     * @return 子プロセッサを処理する場合にはEVAL_BODY_INCLUDE、
     * 子プロセッサの処理をスキップする場合にはSKIP_BODYを返す。
     * ({@link #writeStartElement()}を呼ぶ)
     */
    protected ProcessStatus processStart(Page topLevelPage) {
        if (_childEvaluation) {
            return ProcessStatus.EVAL_BODY_BUFFERED;
        }
        return writeStartElement();
    }

    public boolean isChildEvaluation() {
        return _childEvaluation;
    }

    public void setBodyContent(CycleWriter body) {
        if (_childEvaluation) {
            if (body == null) {
                throw new IllegalArgumentException();
            }
            ProcesstimeInfo info = peekProcesstimeInfo();
            info.setBody(body);
        }
    }

    public void doInitChildProcess() {
        // do nothing.
    }

    public boolean isIteration() {
        return false;
    }

    public ProcessStatus doAfterChildProcess() {
        return ProcessStatus.SKIP_BODY;
    }

    /**
     * このメソッドをoverrideしないこと。
     * 代わりに{@link #processEnd()}をoverrideすること。
     */
    public ProcessStatus doEndProcess() {
        try {
            return processEnd();
        } finally {
            if (_childEvaluation) {
                popProcesstimeInfo();
            }
        }
    }

    /**
     * 子クラスはdoEndProcessではなくこのメソッドをoverrideすること。
     * ProcesstimeInfoを取得するには{@link #peekProcesstimeInfo()}を使用します。
     *
     * @return ページのこのタグ以降を処理する場合にはEVAL_PAGE、
     * 以降の処理をスキップする場合にはSKIP_PAGE。
     */
    protected ProcessStatus processEnd() {
        if (_childEvaluation) {
            writeStartElement();
            ProcesstimeInfo info = peekProcesstimeInfo();
            CycleWriter body = info.getBody();
            if (body != null) {
                writeBody(body.getString());
            }
        }
        writeEndElement();
        return ProcessStatus.EVAL_PAGE;
    }

    // for serialize

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        clearProcesstimeInfo();
    }

    //helper class, methods ----------------------------------------

    protected static class ProcesstimeInfo {

        private CycleWriter _body;
        private List _processtimeProperties;

        public void setBody(CycleWriter body) {
            if (body == null) {
                throw new IllegalArgumentException();
            }
            _body = body;
        }

        public CycleWriter getBody() {
            return _body;
        }

        public boolean hasProcesstimeProperty(ProcessorProperty property) {
            if (property == null) {
                throw new IllegalArgumentException();
            }
            if (_processtimeProperties == null) {
                return false;
            }
            return _processtimeProperties.contains(property);
        }

        public void addProcesstimeProperty(ProcessorProperty property) {
            if (property == null) {
                throw new IllegalArgumentException();
            }
            if (_processtimeProperties == null) {
                _processtimeProperties = new ArrayList();
            }
            if (_processtimeProperties.contains(property) == false) {
                _processtimeProperties.add(property);
            }
        }

        public Iterator iterateProcesstimeProperties() {
            if (_processtimeProperties == null) {
                return NullIterator.getInstance();
            }
            return _processtimeProperties.iterator();
        }

    }

}

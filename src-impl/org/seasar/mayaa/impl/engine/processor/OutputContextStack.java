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

import java.util.ArrayDeque;
import java.util.Deque;

import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.cycle.DefaultCycleLocalInstantiator;

/**
 * 描画中の出力コンテキストをスタックで保持する。
 */
public final class OutputContextStack {

    private static final String OUTPUT_CONTEXT_STACK_KEY =
            OutputContextStack.class.getName() + "#outputContextStack";

    private static final String AUTO_ESCAPE_SUPPRESS_DEPTH_KEY =
            OutputContextStack.class.getName() + "#autoEscapeSuppressDepth";

    static {
        CycleUtil.registVariableFactory(OUTPUT_CONTEXT_STACK_KEY,
                new DefaultCycleLocalInstantiator() {
                    public Object create(Object[] params) {
                        return new ArrayDeque<OutputContext>();
                    }
                });
        CycleUtil.registVariableFactory(AUTO_ESCAPE_SUPPRESS_DEPTH_KEY,
                new DefaultCycleLocalInstantiator() {
                    public Object create(Object[] params) {
                        return new int[]{0};
                    }
                });
    }

    private OutputContextStack() {
        // utility class
    }

    @SuppressWarnings("unchecked")
    private static Deque<OutputContext> getStack() {
        return (Deque<OutputContext>) CycleUtil.getGlobalVariable(OUTPUT_CONTEXT_STACK_KEY, null);
    }

    private static int[] getSuppressDepth() {
        return (int[]) CycleUtil.getGlobalVariable(AUTO_ESCAPE_SUPPRESS_DEPTH_KEY, null);
    }

    public static void push(OutputContext context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        getStack().push(context);
    }

    public static OutputContext pop() {
        Deque<OutputContext> stack = getStack();
        if (stack.isEmpty()) {
            return OutputContext.HTML_BODY;
        }
        return stack.pop();
    }

    public static OutputContext current() {
        Deque<OutputContext> stack = getStack();
        if (stack.isEmpty()) {
            return OutputContext.HTML_BODY;
        }
        return stack.peek();
    }

    /**
     * WriteProcessor がボディ収集中であることを示す抑制をプッシュする。
     * CharactersProcessor の自動エスケープを無効化するために使用する。
     */
    public static void pushSuppressAutoEscape() {
        getSuppressDepth()[0]++;
    }

    /**
     * WriteProcessor のボディ収集終了時に抑制をポップする。
     */
    public static void popSuppressAutoEscape() {
        int[] depth = getSuppressDepth();
        if (depth[0] > 0) {
            depth[0]--;
        }
    }

    /**
     * 現在 autoEscape が抑制されているかどうかを返す。
     * WriteProcessor のボディバッファ収集中は true になる。
     */
    public static boolean isAutoEscapeSuppressed() {
        return getSuppressDepth()[0] > 0;
    }
}

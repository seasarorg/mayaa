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
package org.seasar.mayaa.impl.cycle.script;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.cycle.script.CompiledScript;
import org.seasar.mayaa.cycle.script.ScriptEnvironment;
import org.seasar.mayaa.engine.specification.NodeTreeWalker;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.StringUtil;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 */
public class ScriptUtil {

    private ScriptUtil() {
        // no instantiation.
    }

    public static CompiledScript compile(String text, Class expectedClass) {
        if (expectedClass == null) {
            throw new IllegalArgumentException();
        }
        CompiledScript compiled;
        if (StringUtil.hasValue(text)) {
            ScriptEnvironment environment = ProviderUtil.getScriptEnvironment();
            ServiceCycle cycle = CycleUtil.getServiceCycle();
            NodeTreeWalker node = cycle.getInjectedNode();
            compiled = environment.compile(text, node);
        } else {
            compiled = LiteralScript.NULL_LITERAL_SCRIPT;
        }
        compiled.setExpectedClass(expectedClass);
        return compiled;
    }

    public static String getBlockSignedText(String text) {
        if (StringUtil.isEmpty(text)) {
            return text;
        }
        String blockSign = ProviderUtil.getScriptEnvironment().getBlockSign();
        return blockSign + "{" + text.trim() + "\n}";
    }

    public static void assertSingleScript(String text) {
        String blockSign = ProviderUtil.getScriptEnvironment().getBlockSign();
        ScriptBlockIterator iterator =
                new ScriptBlockIterator(text, blockSign, false);
        if (iterator.hasNext() == false) {
            // no script
            return;
        }

        iterator.next();
        if (iterator.hasNext()) {
            throw new UnbalancedBraceException(text, iterator.getOffset());
        }
    }

    /**
     * {@link ScriptEnvironment#isEmpty(Object)}への委譲。
     * @param scriptResult 判定するオブジェクト
     * @return スクリプト的に空と見なせるなら{@code true}
     */
    public static boolean isEmpty(Object scriptResult) {
        if (ProviderUtil.isInitialized()) {
            return ProviderUtil.getScriptEnvironment().isEmpty(scriptResult);
        }
        return scriptResult == null;
    }

}

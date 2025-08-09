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
package org.seasar.mayaa.impl;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.seasar.mayaa.cycle.ServiceCycle;
import org.seasar.mayaa.engine.Engine;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.cycle.CycleUtil;
import org.seasar.mayaa.impl.engine.EngineUtil;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;
import org.seasar.mayaa.impl.provider.ProviderUtil;
import org.seasar.mayaa.impl.util.ObjectUtil;

/**
 * StrutsなどMayaa以外と組み合わせる上で必ず必要となる。
 * Mayaaへforwardするサーブレットには全て
 * このフィルタを適用する必要がある。
 * （より厳密にはforwardされるmayaaプロセスからcycleを参照している場合の意味）
 * <p>
 * パラメータ"handleException"に"true","yes","y","on","1"のいずれかをセットすると、
 * 発生した例外をMayaaのエラー処理に回します。
 * </p>
 * @author Taro Kato (Gluegent, Inc.)
 */
public class MayaaApplicationFilter implements Filter {

    private static boolean _handleException;

    public void init(FilterConfig filterConfig) {
        _handleException =
            ObjectUtil.booleanValue(
                    filterConfig.getInitParameter("handleException"), false);
    }

    public void destroy() {
        // no operation
    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        CycleUtil.initialize(request, response);
        try {
            chain.doFilter(request, response);
        } catch (IOException t) {
            if (_handleException) {
                doErrorHandle(t);
            } else {
                throw t;
            }
        } catch (ServletException t) {
            if (_handleException) {
                doErrorHandle(t);
            } else {
                throw t;
            }
        } catch (Throwable t) {
            if (_handleException) {
                doErrorHandle(t);
            } else {
                throw new ServletException(t);
            }
        } finally {
            CycleUtil.cycleFinalize();
        }
    }

    /**
     * Mayaaのエラー処理に任せる
     * @param throwable 対象とする例外
     * @throws ServletException エラー処理の過程で例外が発生した場合。
     */
    protected void doErrorHandle(Throwable throwable) throws ServletException {
        try {
            if (EngineUtil.isClientAbortException(throwable)) {
                // client abort は出力しようがないので無視
                return;
            }
            Throwable handled = throwable;
            if (throwable.getCause() != null) {
                if (throwable instanceof ServletException) {
                    handled = throwable.getCause();
                }
            }

            ServiceCycle cycle = CycleUtil.getServiceCycle();
            cycle.getResponse().clearBuffer();

            Engine engine = ProviderUtil.getEngine();
            Specification defaultSpec = SpecificationUtil.getDefaultSpecification();

            cycle.setOriginalNode(defaultSpec);
            cycle.setInjectedNode(defaultSpec);

            SpecificationUtil.initScope();
            cycle.setHandledError(handled);
            engine.getErrorHandler().doErrorHandle(handled, true);
        } catch (Throwable t) {
            if (t instanceof ServletException) {
                throw (ServletException) t;
            }
            throw new ServletException(t);
        }
    }

}

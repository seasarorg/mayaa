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
package org.seasar.mayaa.impl.engine.specification.serialize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.mayaa.engine.specification.Specification;
import org.seasar.mayaa.impl.engine.specification.SpecificationUtil;

/**
 * @author Mitsutaka Watanabe
 */
public class SerializeExecutor {
    private static final Log LOG = LogFactory.getLog(SerializeExecutor.class);

    private ExecutorService _executorService;

    /**
      * ExecutorServiceをアプリケーションコンテナまたはローカルから取得する。
      * @return ScheduledExecutorServiceインスタンス
      */
    private ExecutorService executorService() {
        try {
            final String NAME = "java:comp/DefaultManagedExecutorService";
            final ManagedExecutorService managedExecutor = InitialContext.doLookup(NAME);
            LOG.info("mayaa.ManagedExecutorService aquired");
            return managedExecutor;
        } catch (NamingException e) {
            LOG.info("mayaa.ExecutorService aquired");
            return Executors.newSingleThreadExecutor();
        }
    }

    /**
     * ManagedExecutorServiceでは isShutdown メソッドなどの Lifecycle系のメソッド呼び出しは
     * サポートされていないとのこと。
     * 
     * @param spec シリアライズを実行する対象のSpecificationオブジェクト
     * @return 
     */
    public boolean submit(final Specification spec) {
        if (_executorService == null) {
            _executorService = executorService();
        }
        try {
            _executorService.execute(new Runnable(){
                public void run() {
                    SpecificationUtil.serialize(spec);
                }
            });
        } catch (RejectedExecutionException e) {
            // シャットダウン済みでタスクが追加できなかった時は false
            return false;
        }
        return true;
    }
}


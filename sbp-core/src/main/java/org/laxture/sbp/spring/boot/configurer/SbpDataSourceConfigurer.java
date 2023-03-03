/*
 * Copyright (C) 2019-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.laxture.sbp.spring.boot.configurer;

import org.laxture.sbp.spring.boot.IPluginConfigurer;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SbpDataSourceConfigurer implements IPluginConfigurer {

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
            "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
            "org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration",
            "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration"
        };
    }

    @Override
    public void onBootstrap(SpringBootstrap bootstrap,
                            GenericApplicationContext pluginApplicationContext) {
        // share dataSource
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "dataSource");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "transactionManager");
        // share MongoDbFactory
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "mongoDbFactory");
    }
}

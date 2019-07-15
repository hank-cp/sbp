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
package org.pf4j.spring.boot;

import org.apache.commons.lang3.ArrayUtils;
import org.pf4j.SpringBootPlugin;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Demonstrate how to share additional resources from main {@link ApplicationContext}.
 * In this case, it shared DataSource, TransactionManager and MongoFactory so the
 * plugin could shared database connection resource, eg. connection pool, transaction
 * as main app.
 * <br/>
 * <b>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.</b>
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SharedResourceSpringBootstrap extends SpringBootstrap {

    public SharedResourceSpringBootstrap(SpringBootPlugin plugin,
                                         Class<?>... primarySources) {
        super(plugin, primarySources);
    }

    @Override
    protected String[] getExcludeConfigurations() {
        return ArrayUtils.addAll(super.getExcludeConfigurations(),
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration");
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
        // share dataSource
        registerBeanFromMainContext(applicationContext, "dataSource");
        registerBeanFromMainContext(applicationContext, "transactionManager");
        // share MongoDbFactory
        registerBeanFromMainContext(applicationContext, "mongoDbFactory");

        return applicationContext;
    }

}

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

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.IPluginConfigurer;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Introduce central Jta transaction management between app and plugins, which
 * connect to very different databases with different <code>DataSource</code>.
 *
 * <b>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.</b>
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SbpJtaConfigurer implements IPluginConfigurer {

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
            "org.springframework.boot.autoconfigure.transaction.jta.JtaAutoConfiguration"
        };
    }

    @Override
    public void onBootstrap(SpringBootstrap bootstrap,
                            GenericApplicationContext pluginApplicationContext) {
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "xaDataSourceWrapper");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "transactionManager");
    }
}

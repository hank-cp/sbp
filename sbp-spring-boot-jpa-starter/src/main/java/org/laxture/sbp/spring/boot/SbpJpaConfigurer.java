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
package org.laxture.sbp.spring.boot;

import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * Introduce registrable JPA configuration.
 *
 * <b>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.</b>
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SbpJpaConfigurer implements IPluginConfigurer {

    private final String[] modelPackages;

    public SbpJpaConfigurer(String[] modelPackages) {
        this.modelPackages = modelPackages;
    }

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
            "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
        };
    }

    @Override
    public void onBootstrap(SpringBootstrap bootstrap, GenericApplicationContext pluginApplicationContext) {
        // share jpa beans
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "dataSource");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "org.laxture.sbp.spring.boot.SbpJpaAutoConfiguration");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "transactionManager");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "jpaVendorAdapter");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "persistenceManagedTypes");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "entityManagerFactoryBuilder");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "entityManagerFactory");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "openEntityManagerInViewInterceptorConfigurer");
        bootstrap.importBeanFromMainContext(pluginApplicationContext, "openEntityManagerInViewInterceptor");

        // scan & register model types
        PluginPersistenceManagedTypes persistenceManagedTypes = (PluginPersistenceManagedTypes)
            bootstrap.getMainApplicationContext().getBean("persistenceManagedTypes");
        persistenceManagedTypes.registerPackage(pluginApplicationContext, modelPackages);
        // register classloader
        LocalContainerEntityManagerFactoryBean entityManagerFactory =
            bootstrap.getMainApplicationContext().getBean(LocalContainerEntityManagerFactoryBean.class);
        PluginEntityManagerFactoryBeanRegister.registerClassloader(entityManagerFactory, bootstrap.getClassLoader());
        // rebuild hibernate bootstrap
        entityManagerFactory.afterPropertiesSet();
    }

    // TODO unregister model types on plugin stopped

    @Override
    public void releaseLeaveOverResource(PluginWrapper plugin, GenericApplicationContext mainAppCtx) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory =
            mainAppCtx.getBean(LocalContainerEntityManagerFactoryBean.class);
        PluginEntityManagerFactoryBeanRegister.unregisterClassloader(entityManagerFactory, plugin.getPluginClassLoader());
    }
}

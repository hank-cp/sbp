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

import org.apache.commons.lang3.ArrayUtils;
import org.laxture.sbp.SpringBootPlugin;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;


/**
 * Introduce central Jta transaction management between app and plugins, which
 * connect to very different databases with different <code>DataSource</code>.
 *
 * <b>Note that related AutoConfigurations have to be excluded explicitly to avoid
 * duplicated resource retaining.</b>
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SharedJpaSpringBootstrap extends SpringBootstrap {

    private final String[] modelPackages;

    public SharedJpaSpringBootstrap(SpringBootPlugin plugin,
                                    String[] modelPackages,
                                    Class<?>... primarySources) {
        super(plugin, primarySources);
        this.modelPackages = modelPackages;
    }

    @Override
    protected String[] getExcludeConfigurations() {
        return ArrayUtils.addAll(super.getExcludeConfigurations(),
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration");
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
        // share jpa beans
        importBeanFromMainContext(applicationContext, "dataSource");
        importBeanFromMainContext(applicationContext, "org.laxture.sbp.spring.boot.SbpJpaAutoConfiguration", true);
        importBeanFromMainContext(applicationContext, "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaConfiguration", true);
        importBeanFromMainContext(applicationContext, "transactionManager");
        importBeanFromMainContext(applicationContext, "jpaVendorAdapter");
        importBeanFromMainContext(applicationContext, "persistenceManagedTypes", true);
        importBeanFromMainContext(applicationContext, "entityManagerFactoryBuilder");
        importBeanFromMainContext(applicationContext, "entityManagerFactory", true);
        importBeanFromMainContext(applicationContext, "openEntityManagerInViewInterceptorConfigurer");
        importBeanFromMainContext(applicationContext, "openEntityManagerInViewInterceptor");

        // scan & register model types
        PluginPersistenceManagedTypes persistenceManagedTypes = (PluginPersistenceManagedTypes)
            getMainApplicationContext().getBean("persistenceManagedTypes");
        persistenceManagedTypes.registerPackage(applicationContext, modelPackages);
        // register classloader
        LocalContainerEntityManagerFactoryBean entityManagerFactory =
            getMainApplicationContext().getBean(LocalContainerEntityManagerFactoryBean.class);
        PluginEntityManagerFactoryBeanRegister.registerClassloader(entityManagerFactory, this.getClassLoader());
        // rebuild hibernate bootstrap
        entityManagerFactory.afterPropertiesSet();

        return applicationContext;
    }
}

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
package demo.sbp.library;

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.PluginPersistenceManagedTypes;
import org.laxture.sbp.spring.boot.SharedJpaSpringBootstrap;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class LibraryPlugin extends SpringBootPlugin {

    public LibraryPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected SpringBootstrap createSpringBootstrap() {
        return new SharedJpaSpringBootstrap(
                this, new String[] {"demo.sbp.library.model"}, LibraryPluginStarter.class)
                .importBean("bookService");
    }

    @Override
    public void start() {
        super.start();
//        PluginPersistenceManagedTypes persistenceManagedTypes = (PluginPersistenceManagedTypes)
//            getApplicationContext().getBean("persistenceManagedTypes");
//        BeanFactory beanFactory = (BeanFactory) getApplicationContext().getBean("beanFactory");
//        ResourceLoader resourceLoader = (ResourceLoader) getApplicationContext().getBean("resourceLoader");
//        LocalContainerEntityManagerFactoryBean entityManagerFactory = (LocalContainerEntityManagerFactoryBean)
//            getApplicationContext().getBean("entityManagerFactory");
//
//        persistenceManagedTypes.registerPackage(beanFactory, resourceLoader);
//        entityManagerFactory.afterPropertiesSet();
        // TODO register Classloader

    }

    @Override
    public void stop() {
        // TODO unregister PersistenceManagedTypes
        releaseAdditionalResources();
        super.stop();
    }
}
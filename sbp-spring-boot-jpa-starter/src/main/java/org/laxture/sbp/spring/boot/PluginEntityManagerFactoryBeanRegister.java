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

import org.hibernate.cfg.AvailableSettings;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public final class PluginEntityManagerFactoryBeanRegister {

    public static void registerClassloader(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean, ClassLoader classLoader) {
        //noinspection unchecked
        List<ClassLoader> classLoaders = (List<ClassLoader>)
            entityManagerFactoryBean.getJpaPropertyMap().get(AvailableSettings.CLASSLOADERS);
        if (classLoaders == null) classLoaders = new ArrayList<>();
        if (!classLoaders.contains(classLoader)) classLoaders.add(classLoader);
        entityManagerFactoryBean.getJpaPropertyMap().put(AvailableSettings.CLASSLOADERS, classLoaders);
    }

    public static void unregisterClassloader(LocalContainerEntityManagerFactoryBean entityManagerFactoryBean, ClassLoader classLoader) {
        //noinspection unchecked
        List<ClassLoader> classLoaders = (List<ClassLoader>)
            entityManagerFactoryBean.getJpaPropertyMap().get(AvailableSettings.CLASSLOADERS);
        if (classLoaders == null) classLoaders = new ArrayList<>();
        classLoaders.remove(classLoader);
        entityManagerFactoryBean.getJpaPropertyMap().put(AvailableSettings.CLASSLOADERS, classLoaders);
    }

}

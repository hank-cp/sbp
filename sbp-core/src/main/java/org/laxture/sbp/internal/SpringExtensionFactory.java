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
package org.laxture.sbp.internal;

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.SpringBootPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.GenericApplicationContext;

/**
 * Pf4j ExtensionFactory to create/retrieve extension bean from spring
 *
 * {@link org.springframework.context.ApplicationContext}
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(SpringExtensionFactory.class);

    private SpringBootPluginManager pluginManager;

    public SpringExtensionFactory(SpringBootPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        GenericApplicationContext pluginApplicationContext = getApplicationContext(extensionClass);
        Object extension = null;
        try {
            extension = pluginApplicationContext.getBean(extensionClass);
        } catch (NoSuchBeanDefinitionException ignored) {} // do nothing
        if (extension == null) {
            Object extensionBean = createWithoutSpring(extensionClass);
            pluginApplicationContext.getBeanFactory().registerSingleton(
                    extensionClass.getName(), extensionBean);
            extension = extensionBean;
        }
        //noinspection unchecked
        return (T) extension;
    }

    public String getExtensionBeanName(Class<?> extensionClass) {
        String[] beanNames = getApplicationContext(extensionClass)
                .getBeanNamesForType(extensionClass);
        return beanNames.length > 0 ? beanNames[0] : null;
    }

    private Object createWithoutSpring(Class<?> extensionClass) {
        try {
            return extensionClass.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private GenericApplicationContext getApplicationContext(Class<?> extensionClass) {
        PluginWrapper pluginWrapper = pluginManager.whichPlugin(extensionClass);
        SpringBootPlugin plugin = (SpringBootPlugin) pluginWrapper.getPlugin();
        return plugin.getApplicationContext();
    }
}

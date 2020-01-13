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

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PluginListableBeanFactory extends DefaultListableBeanFactory {

    private ClassLoader classLoader;

    public PluginListableBeanFactory(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected Class<?> predictBeanType(String beanName, RootBeanDefinition mbd, Class<?>... typesToMatch) {
        try {
            return classLoader.loadClass(beanName);
        } catch (ClassNotFoundException ignored) {}
        return super.predictBeanType(beanName, mbd, typesToMatch);
    }

}

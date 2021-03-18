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
package org.laxture.sbp.mock;

import org.pf4j.ExtensionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Pf4j ExtensionFactory to retrieve extension bean from spring context.
 * This is used for plugin unit test mocking.
 *
 * {@link ApplicationContext}
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class MockSpringExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(MockSpringExtensionFactory.class);

    private ApplicationContext applicationContext;

    public MockSpringExtensionFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T create(Class<T> extensionClass) {
        return applicationContext.getBean(extensionClass);
    }
}

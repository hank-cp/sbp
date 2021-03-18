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

import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.springframework.context.ApplicationContext;

/**
 * PluginManager to hold the main ApplicationContext.
 * This is used for plugin unit test mocking.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public class MockSpringBootPluginManager extends DefaultPluginManager {

    private ApplicationContext applicationContext;

    public MockSpringBootPluginManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        ((MockSpringExtensionFactory) getExtensionFactory()).setApplicationContext(applicationContext);
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new MockSpringExtensionFactory(this.applicationContext);
    }
}

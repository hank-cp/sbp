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
package org.laxture.sbp.internal.webflux;

import lombok.extern.slf4j.Slf4j;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.internal.PluginRequestMappingAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public class PluginRequestMappingHandlerMapping extends RequestMappingHandlerMapping
    implements PluginRequestMappingAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void detectHandlerMethods(Object controller) {
        super.detectHandlerMethods(controller);
    }

    @Override
    public void registerController(SpringBootPlugin springBootPlugin, String beanName, Object controller) {
        // unregister RequestMapping if already registered
        unregisterController(springBootPlugin, controller);
        springBootPlugin.registerBeanToMainContext(beanName, controller);
        detectHandlerMethods(controller);
    }

    @Override
    public void unregisterController(SpringBootPlugin springBootPlugin, Object controller) {
        new HashMap<>(getHandlerMethods()).forEach((mapping, handlerMethod) -> {
            if (controller == handlerMethod.getBean()) super.unregisterMapping(mapping);
        });
        springBootPlugin.unregisterBeanFromMainContext(controller);
    }

    @Override
    public Class<?> getRouterFunctionClass() {
        return RouterFunction.class;
    }
}

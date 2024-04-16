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
import org.laxture.sbp.internal.PluginRequestMappingAdapter;
import org.laxture.sbp.spring.boot.IPluginConfigurer;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

/**
 * WebMvc/Webflux plugin configurer for sbp. This is a very fundamental configurer,
 * so it will be applied to every plugin mandatory.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SbpWebConfigurer implements IPluginConfigurer {

    @Override
    public void onStart(SpringBootPlugin plugin) {
        getMainRequestMapping(plugin).registerControllers(plugin);
        getMainRequestMapping(plugin).registerRouterFunction(plugin);
    }

    @Override
    public void onStop(SpringBootPlugin plugin) {
        getMainRequestMapping(plugin).unregisterControllers(plugin);
        getMainRequestMapping(plugin).unregisterRouterFunction(plugin);
    }

    private PluginRequestMappingAdapter getMainRequestMapping(SpringBootPlugin plugin) {
        return (PluginRequestMappingAdapter) // must use beanName here, to support both webmvc & webflux
            plugin.getMainApplicationContext().getBean("requestMappingHandlerMapping");
    }

    @Override
    public void releaseLeaveOverResource(PluginWrapper plugin, GenericApplicationContext mainAppCtx) {
        Stream<Object> stream = mainAppCtx.getBeansWithAnnotation(Controller.class).values().stream();
        stream = Stream.concat(stream, mainAppCtx.getBeansWithAnnotation(RestController.class).values().stream());
        try {
            stream = Stream.concat(stream, mainAppCtx.getBeansOfType(org.springframework.web.servlet.function.RouterFunction.class).values().stream());
        } catch (Throwable ignored) {} // ignore
        try {
            stream = Stream.concat(stream, mainAppCtx.getBeansOfType(org.springframework.web.reactive.function.server.RouterFunction.class).values().stream());
        } catch (Throwable ignored) {} // ignore


        stream.filter(bean -> bean.getClass().getClassLoader() == plugin.getPluginClassLoader())
            .forEach(bean -> {
                SpringBootPlugin.unregisterBeanFromMainContext(mainAppCtx, bean);
            });
    }
}

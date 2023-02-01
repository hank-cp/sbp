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
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public interface PluginRequestMappingAdapter {

    //*************************************************************************
    // RequestMapping
    //*************************************************************************

    void registerController(SpringBootPlugin springBootPlugin, Object controller);

    void unregisterController(SpringBootPlugin springBootPlugin, Object controller);

    default void registerControllers(SpringBootPlugin springBootPlugin) {
        getControllerBeans(springBootPlugin).forEach(bean -> registerController(springBootPlugin, bean));
    }

    default void unregisterControllers(SpringBootPlugin springBootPlugin) {
        getControllerBeans(springBootPlugin).forEach(bean ->
            unregisterController(springBootPlugin, bean));
    }

    default Set<Object> getControllerBeans(SpringBootPlugin springBootPlugin) {
        LinkedHashSet<Object> beans = new LinkedHashSet<>();
        ApplicationContext applicationContext = springBootPlugin.getApplicationContext();
        //noinspection unchecked
        Set<String> sharedBeanNames = (Set<String>) applicationContext.getBean(
                SpringBootstrap.BEAN_IMPORTED_BEAN_NAMES);
        beans.addAll(applicationContext.getBeansWithAnnotation(Controller.class)
                .entrySet().stream().filter(beanEntry -> !sharedBeanNames.contains(beanEntry.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toList()));
        beans.addAll(applicationContext.getBeansWithAnnotation(RestController.class)
                .entrySet().stream().filter(beanEntry -> !sharedBeanNames.contains(beanEntry.getKey()))
                .map(Map.Entry::getValue).collect(Collectors.toList()));
        return beans;
    }

    //*************************************************************************
    // RouterFunction
    //*************************************************************************

    Class<?> getRouterFunctionClass();

    default void registerRouterFunction(SpringBootPlugin springBootPlugin) {
        getRouterFunctionBeans(springBootPlugin).forEach(bean -> {
            String beanName = bean.getClass().getName();
            // unregister RequestMapping if already registered
            springBootPlugin.unregisterBeanFromMainContext(bean);
            springBootPlugin.registerBeanToMainContext(beanName, bean);
        });
        this.initRouterFunctions(springBootPlugin);
    }

    default void unregisterRouterFunction(SpringBootPlugin springBootPlugin) {
        getRouterFunctionBeans(springBootPlugin).forEach(springBootPlugin::unregisterBeanFromMainContext);
        this.initRouterFunctions(springBootPlugin);
    }

    default Set<Object> getRouterFunctionBeans(SpringBootPlugin springBootPlugin) {
        ApplicationContext applicationContext = springBootPlugin.getApplicationContext();
        //noinspection unchecked
        Set<String> sharedBeanNames = (Set<String>) applicationContext.getBean(
            SpringBootstrap.BEAN_IMPORTED_BEAN_NAMES);
        return applicationContext.getBeansOfType(getRouterFunctionClass())
            .entrySet().stream().filter(beanEntry -> !sharedBeanNames.contains(beanEntry.getKey()))
            .map(Map.Entry::getValue).collect(Collectors.toSet());
    }

    default void initRouterFunctions(SpringBootPlugin springBootPlugin) {
        try {
            Object mapping = springBootPlugin.getMainApplicationContext().getBean("routerFunctionMapping");
            Method initMethod;
            try {
                initMethod = mapping.getClass().getDeclaredMethod("initRouterFunctions");
            } catch (NoSuchMethodException e) { return; }
            initMethod.setAccessible(true);
            initMethod.invoke(mapping);
        } catch (BeansException | IllegalAccessException | InvocationTargetException ignored) {}
    }
}

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
package org.laxture.sbp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.laxture.sbp.internal.PluginRequestMappingAdapter;
import org.laxture.sbp.internal.SpringExtensionFactory;
import org.laxture.sbp.internal.webmvc.PluginRequestMappingHandlerMapping;
import org.laxture.sbp.spring.boot.*;
import org.laxture.spring.util.ApplicationContextProvider;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base Pf4j Plugin for Spring Boot.
 *
 * ----
 *
 * ### Following actions will be taken after plugin is started:
 * * Use {@link SpringBootstrap} to initialize Spring environment
 *     in spring-boot style. Some AutoConfiguration need to be excluded explicitly
 *     to make sure plugin resource could be inject to main {@link ApplicationContext}
 * * Share beans from main {@link ApplicationContext} to
 *     plugin {@link ApplicationContext} in order to share resources.
 *     This is done by {@link SpringBootstrap}
 * * Register {@link Controller} and @{@link RestController} beans to
 *     RequestMapping of main {@link ApplicationContext}, so Spring will forward
 *     request to plugin controllers correctly.
 * * Register {@link Extension} to main ApplicationContext
 *
 * ----
 *
 * ### And following actions will be taken when plugin is stopped:
 * * Unregister {@link Extension} in main {@link ApplicationContext}
 * * Unregister controller beans from main RequestMapping
 * * Close plugin {@link ApplicationContext}
 *
 * @see SpringBootstrap
 * @see SharedDataSourceSpringBootstrap
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public abstract class SpringBootPlugin extends Plugin {

    private final SpringBootstrap springBootstrap;
    private ApplicationContext applicationContext;
    private final Set<String> injectedExtensionNames = new HashSet<>();

    public SpringBootPlugin(PluginWrapper wrapper) {
        super(wrapper);
        springBootstrap = createSpringBootstrap();
    }

    private PluginRequestMappingAdapter getMainRequestMapping() {
        return (PluginRequestMappingAdapter)
                getMainApplicationContext().getBean("requestMappingHandlerMapping");
    }

    /**
     * Release plugin holding release on stop.
     */
    public void releaseAdditionalResources() {
    }

    @Override
    public void start() {
        if (getWrapper().getPluginState() == PluginState.STARTED) return;

        long startTs = System.currentTimeMillis();
        log.debug("Starting plugin {} ......", getWrapper().getPluginId());

        applicationContext = springBootstrap.run();
        getMainRequestMapping().registerControllers(this);
        getMainRequestMapping().registerRouterFunction(this);

        // register Extensions
        Set<String> extensionClassNames = getWrapper().getPluginManager()
                .getExtensionClassNames(getWrapper().getPluginId());
        for (String extensionClassName : extensionClassNames) {
            try {
                log.debug("Register extension <{}> to main ApplicationContext", extensionClassName);
                Class<?> extensionClass = getWrapper().getPluginClassLoader().loadClass(extensionClassName);
                SpringExtensionFactory extensionFactory = (SpringExtensionFactory) getWrapper()
                        .getPluginManager().getExtensionFactory();
                Object bean = extensionFactory.create(extensionClass);
                String beanName = extensionFactory.getExtensionBeanName(extensionClass);
                registerBeanToMainContext(beanName, bean);
                injectedExtensionNames.add(beanName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }

        ApplicationContextProvider.registerApplicationContext(applicationContext);
        applicationContext.publishEvent(new SbpPluginStartedEvent(applicationContext));
        if (getPluginManager().isMainApplicationStarted()) {
            // if main application context is not ready, don't send restart event
            applicationContext.publishEvent(new SbpPluginRestartedEvent(applicationContext));
        }

        log.debug("Plugin {} is started in {}ms", getWrapper().getPluginId(), System.currentTimeMillis() - startTs);
    }

    @Override
    public void stop() {
        if (getWrapper().getPluginState() != PluginState.STARTED) return;

        log.debug("Stopping plugin {} ......", getWrapper().getPluginId());
        releaseAdditionalResources();
        // unregister Extension beans
        for (String extensionName : injectedExtensionNames) {
            log.debug("Unregister extension <{}> to main ApplicationContext", extensionName);
            unregisterBeanFromMainContext(extensionName);
        }

        getMainRequestMapping().unregisterControllers(this);
        getMainRequestMapping().unregisterRouterFunction(this);
        applicationContext.publishEvent(new SbpPluginStoppedEvent(applicationContext));
        ApplicationContextProvider.unregisterApplicationContext(applicationContext);
        injectedExtensionNames.clear();
        ((ConfigurableApplicationContext) applicationContext).close();

        log.debug("Plugin {} is stopped", getWrapper().getPluginId());
    }

    /**
     * Clean legacy resources left behind by failed plugin starting.
     *
     * @param plugin
     * @param mainAppCtx
     */
    public static void releaseLegacyResources(PluginWrapper plugin,
                                              GenericApplicationContext mainAppCtx) {
        try {
            // unregister Extension beans
            Set<String> extensionClassNames = plugin.getPluginManager()
                    .getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                    Class<?> extensionClass = plugin.getPluginClassLoader().loadClass(extensionClassName);
                    SpringExtensionFactory extensionFactory = (SpringExtensionFactory) plugin
                            .getPluginManager().getExtensionFactory();
                    String beanName = extensionFactory.getExtensionBeanName(extensionClass);
                    if (StringUtils.isEmpty(beanName)) continue;
                    unregisterBeanFromMainContext(mainAppCtx, beanName);
            }

            // unregister Controller beans
            PluginRequestMappingHandlerMapping requestMapping = (PluginRequestMappingHandlerMapping)
                    mainAppCtx.getBean("requestMappingHandlerMapping");
            Stream.concat(Stream.concat(Stream.concat(
                mainAppCtx.getBeansOfType(Controller.class).values().stream(),
                mainAppCtx.getBeansWithAnnotation(RestController.class).values().stream()),
                mainAppCtx.getBeansOfType(org.springframework.web.servlet.function.RouterFunction.class).values().stream()),
                mainAppCtx.getBeansOfType(org.springframework.web.reactive.function.server.RouterFunction.class).values().stream())
                    .filter(bean -> bean.getClass().getClassLoader() == plugin.getPluginClassLoader())
                    .forEach(bean -> {
                        SpringBootPlugin.unregisterBeanFromMainContext(mainAppCtx, bean);
                    });
        } catch (Exception e) {
            log.trace("Release registered resources failed. "+e.getMessage(), e);
        }
    }

    protected abstract SpringBootstrap createSpringBootstrap();

    public GenericApplicationContext getApplicationContext() {
        return (GenericApplicationContext) applicationContext;
    }

    public SpringBootPluginManager getPluginManager() {
        return (SpringBootPluginManager) getWrapper().getPluginManager();
    }

    public GenericApplicationContext getMainApplicationContext() {
        return (GenericApplicationContext) getPluginManager().getMainApplicationContext();
    }

    public void registerBeanToMainContext(String beanName, Object bean) {
        Assert.notNull(bean, "bean must not be null");
        beanName = StringUtils.isEmpty(beanName) ? bean.getClass().getName() : beanName;
        getMainApplicationContext().getBeanFactory().registerSingleton(beanName, bean);
    }

    public void unregisterBeanFromMainContext(String beanName) {
        unregisterBeanFromMainContext(getMainApplicationContext(), beanName);
        Assert.notNull(beanName, "bean must not be null");
        ((AbstractAutowireCapableBeanFactory) getMainApplicationContext().getBeanFactory()).destroySingleton(beanName);
    }

    public void unregisterBeanFromMainContext(Object bean) {
        unregisterBeanFromMainContext(getMainApplicationContext(), bean);
    }

    public static void unregisterBeanFromMainContext(GenericApplicationContext mainCtx,
                                                     String beanName) {
        Assert.notNull(beanName, "bean must not be null");
        ((AbstractAutowireCapableBeanFactory) mainCtx.getBeanFactory()).destroySingleton(beanName);
    }

    public static void unregisterBeanFromMainContext(GenericApplicationContext mainCtx,
                                                     Object bean) {
        Assert.notNull(bean, "bean must not be null");
        String beanName = bean.getClass().getName();
        ((AbstractAutowireCapableBeanFactory) mainCtx.getBeanFactory()).destroySingleton(beanName);
    }

}

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
package org.pf4j.spring.boot;

import org.pf4j.SpringBootPlugin;
import org.pf4j.SpringBootPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Base plugin {@link ApplicationContext} bootstrap class like {@link SpringApplication}
 * to initialize environment in spring-boot style.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringBootstrap extends SpringApplication {

    private final static Logger log = LoggerFactory.getLogger(SpringBootstrap.class);

    public final static String PLUGIN_BEAN_NAME = "pf4j.plugin";

    private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

    public static final String[] DEFAULT_EXCLUDE_CONFIGURATIONS = {
            "org.pf4j.spring.boot.Pf4jAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration",
            "org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration",
            "org.springframework.boot.autoconfigure.web.servlet.HttpEncodingAutoConfiguration",
            "org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration",
            "org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration",
            "org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
            "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
    };

    private final SpringBootPlugin plugin;

    private final ApplicationContext mainApplicationContext;

    private final ClassLoader pluginClassLoader;

    private final HashSet<String> sharedBeanNames = new HashSet<>();

    private final Map<String, Object> presetProperties = new HashMap<>();

    /**
     * Constructor should be the only thing need to take care for this Class.
     * Generally new an instance and {@link #run(String...)} it
     * in {@link SpringBootPlugin#createSpringBootstrap()} method.
     *
     * @param primarySources {@link SpringApplication} that annotated with {@link SpringBootApplication}
     */
    public SpringBootstrap(SpringBootPlugin plugin,
                           Class<?>... primarySources) {
        super(new DefaultResourceLoader(plugin.getWrapper().getPluginClassLoader()), primarySources);
        this.plugin = plugin;
        this.mainApplicationContext = plugin.getMainApplicationContext();
        this.pluginClassLoader = plugin.getWrapper().getPluginClassLoader();
        Map<String, Object> presetProperties = ((SpringBootPluginManager)
                plugin.getWrapper().getPluginManager()).getPresetProperties();
        if (presetProperties != null) this.presetProperties.putAll(presetProperties);
        this.presetProperties.put(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE,
                getExcludeConfigurations());
        setBannerMode(Banner.Mode.OFF);
    }

    /**
     * Beans that wanted to be shared from main {@link ApplicationContext}.
     * Note that this method only takes effect before {@link #run(String...)} method.
     */
    public SpringBootstrap addSharedBeanName(String beanName) {
        this.sharedBeanNames.add(beanName);
        return this;
    }

    /**
     * Properties that need to be set when this app is started as a plugin.
     * Note that this method only takes effect before {@link #run(String...)} method.
     */
    public SpringBootstrap addPresetProperty(String name, Object value) {
        this.presetProperties.put(name, value);
        return this;
    }

    @Override
    protected void configurePropertySources(ConfigurableEnvironment environment,
                                            String[] args) {
        super.configurePropertySources(environment, args);
        environment.setActiveProfiles("plugin");
        environment.getPropertySources().addLast(new ExcludeConfigurations());
    }

    /** Override this methods to customize excluded spring boot configuration */
    protected String[] getExcludeConfigurations() {
        return DEFAULT_EXCLUDE_CONFIGURATIONS;
    }

    @Override
    public ConfigurableApplicationContext createApplicationContext() {
        setWebApplicationType(WebApplicationType.NONE);
        AnnotationConfigApplicationContext applicationContext =
                (AnnotationConfigApplicationContext) super.createApplicationContext();
//        applicationContext.setParent(mainApplicationContext);
        applicationContext.setClassLoader(pluginClassLoader);

        applicationContext.getBeanFactory().registerSingleton(PLUGIN_BEAN_NAME, plugin);
        applicationContext.getBeanFactory().autowireBean(plugin);

        if (!CollectionUtils.isEmpty(sharedBeanNames)) {
            for (String beanName : sharedBeanNames) {
                registerBeanFromMainContext(applicationContext, beanName);
            }
        }
        return applicationContext;
    }

    protected void registerBeanFromMainContext(AbstractApplicationContext applicationContext,
                                               String beanName) {
        try {
            Object bean = mainApplicationContext.getBean(beanName);
            if (bean != null) {
                applicationContext.getBeanFactory().registerSingleton(beanName, bean);
                applicationContext.getBeanFactory().autowireBean(bean);
                log.info("Bean {} is register from main ApplicationContext", beanName);
            } else {
                log.warn("Bean {} is not found in main ApplicationContext", beanName);
            }
        } catch (NoSuchBeanDefinitionException ex) {
            log.warn("Bean {} is not found in main ApplicationContext", beanName);
        }
    }

    public class ExcludeConfigurations extends MapPropertySource {
        ExcludeConfigurations() {
            super("Exclude Configurations", presetProperties);
        }
    }
}

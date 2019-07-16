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

import org.pf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * Pf4j Configuration for Spring Boot
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see Pf4jProperties
 */
@Configuration
@ConditionalOnClass({ PluginManager.class, SpringBootPluginManager.class })
@ConditionalOnProperty(prefix = Pf4jProperties.PREFIX, value = "enabled", havingValue = "true")
@EnableConfigurationProperties({Pf4jProperties.class, Pf4jPluginProperties.class})
public class Pf4jAutoConfiguration {

	private Logger logger = LoggerFactory.getLogger(Pf4jAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean(PluginStateListener.class)
	public PluginStateListener pluginStateListener() {
		return event -> {
			PluginDescriptor descriptor = event.getPlugin().getDescriptor();
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Plugin [%s（%s）](%s) %s", descriptor.getPluginId(),
						descriptor.getVersion().toString(), descriptor.getPluginDescription(),
						event.getPluginState().toString()));
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean(WebMvcRegistrations.class)
	public WebMvcRegistrations mvcRegistrations() {
		return new WebMvcRegistrations() {
			@Override
			public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
				return new PluginRequestMappingHandlerMapping();
			}

			@Override
			public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
				return null;
			}

			@Override
			public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver() {
				return null;
			}
		};
	}

	@Bean
	@ConditionalOnMissingBean
	public PluginManager pluginManager(Pf4jProperties properties,
									   Pf4jPluginProperties pluginProperties) {
		// Setup RuntimeMode
		System.setProperty("pf4j.mode", properties.getRuntimeMode().toString());
		
		// Setup Plugin folder
		String pluginsRoot = StringUtils.hasText(properties.getPluginsRoot()) ? properties.getPluginsRoot() : "plugins";
		System.setProperty("pf4j.pluginsDir", pluginsRoot);
		String appHome = System.getProperty("app.home");
		if (RuntimeMode.DEPLOYMENT == properties.getRuntimeMode()
				&& StringUtils.hasText(appHome)) {
			System.setProperty("pf4j.pluginsDir", appHome + File.separator + pluginsRoot);
		}

		SpringBootPluginManager pluginManager = new SpringBootPluginManager(
				new File(pluginsRoot).toPath()) {
			@Override
			protected void initialize() {
				super.initialize();
				if (properties.getClassesDirectories() != null && properties.getClassesDirectories().size() > 0) {
					for (String classesDirectory : properties.getClassesDirectories()) {
						pluginClasspath.addClassesDirectories(classesDirectory);
					}
				}
				if (properties.getLibDirectories() != null && properties.getLibDirectories().size() > 0) {
					for (String libDirectory : properties.getLibDirectories()) {
						pluginClasspath.addLibDirectories(libDirectory);
					}
				}
			}

			@Override
			protected PluginLoader createPluginLoader() {
				return new CompoundPluginLoader()
					.add(new DefaultPluginLoader(this, pluginClasspath) {
						@Override
						protected PluginClassLoader createPluginClassLoader(Path pluginPath,
																			PluginDescriptor pluginDescriptor) {
							return new SpringBootPluginClassLoader(pluginManager,
									pluginDescriptor, getClass().getClassLoader());
						}
					})
					.add(new JarPluginLoader(this));
			}
		};

		pluginManager.presetProperties(flatProperties(pluginProperties.properties));
		pluginManager.setExactVersionAllowed(properties.isExactVersionAllowed());
		pluginManager.setSystemVersion(properties.getSystemVersion());
		pluginManager.setAutoStartPlugin(properties.isAutoStartPlugin());

		return pluginManager;
	}

	private Map<String, Object> flatProperties(Map<String, Object> propertiesMap) {
		Stack<String> pathStack = new Stack<>();
		Map<String, Object> flatMap = new HashMap<>();
		propertiesMap.entrySet().forEach(mapEntry -> {
			recurse(mapEntry, entry -> {
				pathStack.push(entry.getKey());
				if (entry.getValue() instanceof Map) return;
				flatMap.put(String.join(".", pathStack), entry.getValue());

			}, entry -> {
				pathStack.pop();
			});
		});
		return flatMap;
	}

	private void recurse(Map.Entry<String, Object> entry,
						 Consumer<Map.Entry<String, Object>> preConsumer,
						 Consumer<Map.Entry<String, Object>> postConsumer) {
		preConsumer.accept(entry);

		if (entry.getValue() instanceof Map) {
			Map<String, Object> entryMap = (Map<String, Object>) entry.getValue();
			for (Map.Entry<String, Object> subEntry : entryMap.entrySet()) {
				recurse(subEntry, preConsumer, postConsumer);
			}
		}

		postConsumer.accept(entry);
	}

}
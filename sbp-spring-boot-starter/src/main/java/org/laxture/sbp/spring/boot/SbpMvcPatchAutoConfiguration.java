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
package org.laxture.sbp.spring.boot;

import org.laxture.sbp.SpringBootPluginManager;
import org.laxture.sbp.internal.PluginRequestMappingHandlerMapping;
import org.pf4j.PluginManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Sbp main app auto configuration for Spring Boot
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 * @see SbpProperties
 */
@Configuration
@ConditionalOnClass({ PluginManager.class, SpringBootPluginManager.class })
@ConditionalOnProperty(prefix = SbpProperties.PREFIX, value = "enabled", havingValue = "true")
public class SbpMvcPatchAutoConfiguration {

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

}
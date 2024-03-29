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
package org.laxture.sbp.spring.boot.webflux;

import org.laxture.sbp.SpringBootPlugin;
import org.springdoc.core.conditions.MultipleOpenApiSupportCondition;
import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springdoc.webflux.api.MultipleOpenApiWebFluxResource;
import org.springdoc.webflux.core.configuration.MultipleOpenApiSupportConfiguration;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;

import java.util.List;

import static org.springdoc.core.utils.Constants.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
@Conditional(MultipleOpenApiSupportCondition.class)
@AutoConfigureBefore(MultipleOpenApiSupportConfiguration.class)
@ConditionalOnMissingBean(SpringBootPlugin.class) // only configure for app
public class SbpSpringDocWebFluxAppAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean @Primary
    @ConditionalOnProperty(name = SPRINGDOC_USE_MANAGEMENT_PORT, havingValue = "false", matchIfMissing = true)
    @Lazy(false)
    MultipleOpenApiWebFluxResource multipleOpenApiResource(
            ApplicationContext applicationContext,
            List<GroupedOpenApi> groupedOpenApis,
            ObjectFactory<OpenAPIService> defaultOpenAPIBuilder, AbstractRequestService requestBuilder,
            GenericResponseService responseBuilder, OperationService operationParser,
            SpringDocConfigProperties springDocConfigProperties,
            SpringDocProviders springDocProviders,
            SpringDocCustomizers springDocCustomizers) {
        return new RegistrableMultipleOpenApiWebFluxResource(
            applicationContext,
            groupedOpenApis,
            defaultOpenAPIBuilder, requestBuilder,
            responseBuilder, operationParser,
            springDocConfigProperties,
            springDocProviders,
            springDocCustomizers);
    }

}

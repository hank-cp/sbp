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

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.util.BeanUtil;
import org.pf4j.PluginWrapper;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.service.OpenAPIService;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SbpSpringDocWebFluxConfigurer implements IPluginConfigurer {

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
            "org.springdoc.core.configuration.SpringDocConfiguration",
            "org.springdoc.core.properties.SpringDocConfigProperties",
            "org.springdoc.core.configuration.SpringDocJavadocConfiguration",
            "org.springdoc.core.configuration.SpringDocGroovyConfiguration",
            "org.springdoc.core.configuration.SpringDocSecurityConfiguration",
            "org.springdoc.core.configuration.SpringDocFunctionCatalogConfiguration",
            "org.springdoc.core.configuration.SpringDocNativeConfiguration",
            "org.springdoc.core.configuration.SpringDocHateoasConfiguration",
            "org.springdoc.core.configuration.SpringDocPageableConfiguration",
            "org.springdoc.core.configuration.SpringDocSortConfiguration",
            "org.springdoc.core.configuration.SpringDocDataRestConfiguration",
            "org.springdoc.core.configuration.SpringDocKotlinConfiguration",
            "org.springdoc.core.configuration.SpringDocKotlinxConfiguration",
            "org.springdoc.webflux.core.configuration.SpringDocWebFluxConfiguration",
            "org.springdoc.webflux.core.configuration.MultipleOpenApiSupportConfiguration"
        };
    }

    @Override
    public void onStart(SpringBootPlugin plugin) {
        if (plugin.getMainApplicationContext().getBeanNamesForType(RegistrableMultipleOpenApiWebFluxResource.class).length > 0) {
            RegistrableMultipleOpenApiWebFluxResource openApiResource =
                plugin.getMainApplicationContext().getBean(RegistrableMultipleOpenApiWebFluxResource.class);
            GroupedOpenApi groupedOpenApi = plugin.getApplicationContext()
                .getBean(GroupedOpenApi.class);
            openApiResource.registerPlugin(plugin, groupedOpenApi);
        }
        refreshCacheIfNeeded(plugin.getMainApplicationContext());
    }

    @Override
    public void onStop(SpringBootPlugin plugin) {
        if (plugin.getMainApplicationContext().getBeanNamesForType(RegistrableMultipleOpenApiWebFluxResource.class).length > 0) {
            RegistrableMultipleOpenApiWebFluxResource openApiResource =
                plugin.getMainApplicationContext().getBean(RegistrableMultipleOpenApiWebFluxResource.class);
            GroupedOpenApi groupedOpenApi = plugin.getApplicationContext()
                .getBean(GroupedOpenApi.class);
            openApiResource.unregisterPlugin(groupedOpenApi.getGroup());
        }
        refreshCacheIfNeeded(plugin.getMainApplicationContext());
    }

    public void refreshCacheIfNeeded(GenericApplicationContext applicationContext) {
        SpringDocConfigProperties springDocConfigProperties =
            applicationContext.getBean(SpringDocConfigProperties.class);
        if (!springDocConfigProperties.isCacheDisabled()) {
            OpenAPIService openApiService =
                applicationContext.getBean(OpenAPIService.class);
            BeanUtil.<Map<?, ?>>getFieldValue(openApiService, "cachedOpenAPI").clear();
        }
    }

    @Override
    public void releaseLegacyResource(PluginWrapper plugin, GenericApplicationContext mainAppCtx) {
        // TODO
    }
}

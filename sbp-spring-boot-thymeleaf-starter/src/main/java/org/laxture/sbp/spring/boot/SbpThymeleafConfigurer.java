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

import org.laxture.sbp.util.BeanUtil;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.support.GenericApplicationContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.util.Set;

/**
 * Plugin configurer for Thymeleaf.
 * To customize Thymeleaf configuration via application properties,
 * you need to import <@link ThymeleafProperties> explicitly by
 * <code>@Import(ThymeleafProperties.class)</code>
 */
public class SbpThymeleafConfigurer implements IPluginConfigurer {

    private SpringResourceTemplateResolver pluginTemplateResolver;

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
                "org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration"
        };
    }

    @Override
    public void afterBootstrap(SpringBootstrap bootstrap, GenericApplicationContext pluginApplicationContext) {
        SpringTemplateEngine templateEngine = (SpringTemplateEngine) bootstrap.getMainApplicationContext()
                .getBean("templateEngine");
        Set<ITemplateResolver> resolvers = BeanUtil.getFieldValue(templateEngine, "templateResolvers");
        pluginTemplateResolver = createPluginTemplateResolver(pluginApplicationContext);
        assert resolvers != null;
        resolvers.add(this.pluginTemplateResolver);
    }

    private SpringResourceTemplateResolver createPluginTemplateResolver(GenericApplicationContext pluginApplicationContext) {
        ThymeleafProperties properties;
        try {
            properties = pluginApplicationContext.getBean(ThymeleafProperties.class);
        } catch (NoSuchBeanDefinitionException ignored) {
            properties = new ThymeleafProperties();
        }
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(pluginApplicationContext);
        resolver.setPrefix(properties.getPrefix());
        resolver.setSuffix(properties.getSuffix());
        resolver.setTemplateMode(properties.getMode());
        if (properties.getEncoding() != null) {
            resolver.setCharacterEncoding(properties.getEncoding().name());
        }
        resolver.setCacheable(properties.isCache());
        Integer order = properties.getTemplateResolverOrder();
        if (order != null) {
            resolver.setOrder(order);
        }
        resolver.setCheckExistence(properties.isCheckTemplate());
        return resolver;
    }

    @Override
    public void releaseLeaveOverResource(PluginWrapper plugin, GenericApplicationContext mainAppCtx) {
        if (this.pluginTemplateResolver != null) {
            SpringTemplateEngine templateEngine = (SpringTemplateEngine) mainAppCtx.getBean("templateEngine");
            Set<ITemplateResolver> resolvers = BeanUtil.getFieldValue(templateEngine, "templateResolvers");
            assert resolvers != null;
            resolvers.remove(this.pluginTemplateResolver);
        }
    }
}

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

import org.flywaydb.core.Flyway;
import org.pf4j.SpringBootPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Configuration
@AutoConfigureAfter(FlywayAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@ConditionalOnBean(SpringBootPlugin.class)
public class FlywayClassLoaderConfiguration {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private SpringBootPlugin plugin;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    public FlywayMigrationStrategy migrationStrategy() {
        return new FlywayClassLoaderPatch(plugin);
    }

    public static class FlywayClassLoaderPatch implements FlywayMigrationStrategy {

        private SpringBootPlugin plugin;

        public FlywayClassLoaderPatch(SpringBootPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void migrate(Flyway flyway) {
            flyway.setClassLoader(plugin.getWrapper().getPluginClassLoader());
            flyway.migrate();
        }
    }
}

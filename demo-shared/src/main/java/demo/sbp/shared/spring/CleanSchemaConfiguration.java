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
package demo.sbp.shared.spring;

import org.flywaydb.core.Flyway;
import org.laxture.sbp.SpringBootPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * DataImporter by Flyway, for dev/integration test only.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Configuration
@AutoConfigureAfter(FlywayAutoConfiguration.class)
public class CleanSchemaConfiguration {

    @Autowired(required = false)
    private SpringBootPlugin plugin;

    @Bean
    @ConditionalOnProperty(prefix = "spring.flyway", name = "clean-before-migrate")
    public FlywayMigrationStrategy migrationStrategy() {
        return new FlywaySchemaCleaner(plugin);
    }

    public static class FlywaySchemaCleaner implements FlywayMigrationStrategy {

        private SpringBootPlugin plugin;

        public FlywaySchemaCleaner(SpringBootPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void migrate(Flyway flyway) {
            try (Statement stat = flyway.getDataSource().getConnection().createStatement()) {
                for (String schema : flyway.getSchemas()) {
                    stat.execute("DROP SCHEMA IF EXISTS "+schema + " CASCADE");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (plugin != null) {
                flyway.setClassLoader(plugin.getWrapper().getPluginClassLoader());
            }
            flyway.migrate();
        }
    }
}

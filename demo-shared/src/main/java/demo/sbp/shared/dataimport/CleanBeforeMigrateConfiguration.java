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
package demo.sbp.shared.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.ApplicationContext;
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
@Slf4j
public class CleanBeforeMigrateConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty("spring.flyway.clean-before-migrate")
    public FlywayMigrationStrategy migrationStrategy() {
        return flyway -> {
            try (Statement stat = flyway.getConfiguration().getDataSource().getConnection().createStatement()) {
                for (String schema : flyway.getConfiguration().getSchemas()) {
                    stat.execute("DROP SCHEMA IF EXISTS "+schema + " CASCADE");
                    log.info("schema \"{}\" is dropped", schema);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            if (applicationContext.getClassLoader() !=
                    flyway.getConfiguration().getClassLoader()) {
                FluentConfiguration alterConfiguration = Flyway.configure(applicationContext.getClassLoader());
                alterConfiguration.configuration(flyway.getConfiguration());
                new Flyway(alterConfiguration).migrate();
            } else {
                flyway.migrate();
            }
        };
    }
}

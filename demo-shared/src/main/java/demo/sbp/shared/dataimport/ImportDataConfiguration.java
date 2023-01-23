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

import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DataImporter by Flyway, for dev/integration test only.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Configuration
public class ImportDataConfiguration {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnProperty(value = {
        "spring.flyway.enabled",
        "spring.flyway.import-data.enabled"
    })
    @DependsOn("flywayInitializer")
    public List<Flyway> flywayExecutor() {
        List<Flyway> executors = new ArrayList<>();
        String location = null;
        int i = 0;
        do {
            location = getLocationProperties(applicationContext.getEnvironment(), i++);
            if (location != null) {
                Flyway importDataFlyway = getFlywayConfiguration(null, location).load();
                importDataFlyway.repair();
                importDataFlyway.migrate();
            }
        } while (location != null);


        String[] config = null;
        int j = 0;
        do {
            config = getConfigProperties(applicationContext.getEnvironment(), j++);
            if (config != null) {
                Flyway importDataFlyway = getFlywayConfiguration(config[0], config[1]).load();
                importDataFlyway.repair();
                importDataFlyway.migrate();
            }
        } while (config != null);

        return executors;
    }

    private FluentConfiguration getFlywayConfiguration(String schema, String location) {
        FluentConfiguration configuration = new FluentConfiguration(
            applicationContext.getClassLoader());
        String[] locationParts = location.split(":");
        String tableName = "_db_" + (locationParts.length > 1 ? locationParts[locationParts.length-1] : location);
        if (StringUtils.isEmpty(schema)) {
            schema = applicationContext.getEnvironment().getProperty("spring.flyway.schemas[0]");
        }
        configuration.dataSource(dataSource);
        configuration.schemas(schema);
        configuration.locations(location);
        configuration.table(tableName);
        configuration.baselineOnMigrate(true);
        configuration.baselineVersion("0");
        return configuration;
    }

    private String getLocationProperties(Environment env, int index) {
        String prop = env.getProperty(String.format("spring.flyway.import-data.location[%s]", index));
        if (prop == null) prop = env.getProperty(String.format("spring.flyway.import-data.location.%s", index));
        return prop;
    }

    private String[] getConfigProperties(Environment env, int index) {
        String schema = env.getProperty(String.format("spring.flyway.import-data.config[%s].schema", index));
        String location = env.getProperty(String.format("spring.flyway.import-data.config[%s].location", index));
        if (StringUtils.isEmpty(schema) || StringUtils.isEmpty(location)) return null;
        return new String[] { schema, location };
    }
}

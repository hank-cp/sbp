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

import demo.sbp.shared.FlywayDataImporter;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.laxture.sbp.SpringBootPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;

/**
 * DataImporter by Flyway, for dev/integration test only.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Configuration
@AutoConfigureAfter(FlywayAutoConfiguration.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class ImportDataConfiguration {

    @Autowired
    private Flyway flyway;

    @Autowired(required = false)
    private SpringBootPlugin plugin;

    @Bean
    @ConditionalOnProperty(prefix = "spring.flyway", name = "import-data")
    @DependsOn("flywayInitializer")
    public FlywayDataImporter flywayDataImporter() {
        FluentConfiguration flywayConf = plugin != null
                ? Flyway.configure(plugin.getWrapper().getPluginClassLoader()) : Flyway.configure();
        flywayConf.configuration(flyway.getConfiguration());
        flywayConf.baselineVersion("0");
        flywayConf.baselineOnMigrate(true);
        flywayConf.locations("classpath:/db_data");
        flywayConf.table("_db_data");
        Flyway importDataFlyway = new Flyway(flywayConf);
        FlywayDataImporter importer = new FlywayDataImporter(importDataFlyway);
        importer.setOrder(Ordered.LOWEST_PRECEDENCE);
        return importer;
    }
}

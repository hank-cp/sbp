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

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.core.Ordered;

/**
 * DataImporter by Flyway, for dev/integration test only.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class FlywayExecutor implements InitializingBean, Ordered {

    private final Flyway flyway;

    private int order = Ordered.LOWEST_PRECEDENCE;

    /**
     * Create a new {@link FlywayMigrationInitializer} instance.
     * @param flyway the flyway instance
     */
    public FlywayExecutor(Flyway flyway) {
        this.flyway = flyway;
    }

    @Override
    public void afterPropertiesSet() {
        this.flyway.migrate();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}

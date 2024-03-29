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
package demo.sbp.webflux.admin;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SpringBootApplication
public class AdminPluginStarter {

    public static void main(String[] args) {
        SpringApplication.run(AdminPluginStarter.class, args);
    }

    @Bean
    RouterFunction<ServerResponse> adminPluginRoute() {
        return route().GET("/admin/plugin",
            this::iAmPlugin,
            ops -> ops.operationId("iAmPlugin")).build();
        // ref: https://github.com/springdoc/springdoc-openapi/blob/master/springdoc-openapi-webflux-core/src/test/java/test/org/springdoc/api/app90/HelloRouter.java
    }

    @Bean
    public GroupedOpenApi adminApiDoc() {
        return GroupedOpenApi.builder().group("admin")
            .packagesToScan("demo.sbp.webflux.admin")
            .build();
    }

    private Mono<ServerResponse> iAmPlugin(ServerRequest req) {
        return ok().body(Mono.just("I am plugin!"), String.class);
    }
}

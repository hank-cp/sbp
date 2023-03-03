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
package demo.sbp.app;

import io.swagger.v3.oas.annotations.Operation;
import org.laxture.spring.util.ApplicationContextProvider;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SpringBootApplication(scanBasePackages = "demo.sbp", exclude = {
    SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class,
})
@ComponentScans(value = {
    @ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX, pattern = "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration\\$ResourceChainCustomizerConfiguration"))
})
@Profile("no_security")
public class DemoWebFlux {

    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        builder.profiles("no_security")
            .sources(DemoWebFlux.class)
            .web(WebApplicationType.REACTIVE).build().run();
    }

    @Bean
    public ApplicationContextAware multiApplicationContextProviderRegister() {
        return ApplicationContextProvider::registerApplicationContext;
    }

    @Bean
    @RouterOperation(operation = @Operation(operationId = "/book/authors"))
    RouterFunction<ServerResponse> bookAuthorsRoute() {
        return route(GET("/book/authors"),
            req -> ok().body(Mono.just("George Orwell"), String.class));
    }

    @Bean
    public GroupedOpenApi mainApiDoc() {
        return GroupedOpenApi.builder().group("main")
            .packagesToScan("demo.sbp.app")
            .build();
    }

}
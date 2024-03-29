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

import lombok.extern.java.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoTestWebFlux.class)
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration")
@AutoConfigureWebTestClient
@ActiveProfiles("no_security")
@Log
public class PluginIntegrationTest {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testApp() throws Exception {
        webTestClient.
            get().uri("/book/list")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$").value(hasSize(3));
    }

    @Test
    public void testPlugin() {
        webTestClient
            .get().uri("/admin/user")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$").value(equalTo("Hello User!"));
    }

    @Test
    public void testResourceHandler() throws Exception {
        webTestClient
            .get().uri("/public/foo.html")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(response -> {
                assertThat(new String(response.getResponseBody(), Charset.defaultCharset()),
                    equalTo("<body>\nHello foo!\n</body>"));
            });

        webTestClient
            .get().uri("/public/bar.html")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(response -> {
                assertThat(new String(response.getResponseBody(), Charset.defaultCharset()),
                    equalTo("<body>\nHello bar!\n</body>"));
            });

        // stop demo-plugin-admin
        pluginManager.stopPlugin("demo-plugin-webflux");

        webTestClient
            .get().uri("/public/bar.html")
            .exchange()
            .expectStatus().isNotFound();

        // start demo-plugin-admin again
        pluginManager.startPlugin("demo-plugin-webflux");

        // bar.html should come back to live again.
        webTestClient
            .get().uri("/public/bar.html")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith(response -> {
                assertThat(new String(response.getResponseBody(), Charset.defaultCharset()),
                    equalTo("<body>\nHello bar!\n</body>"));
            });
    }

    @Test
    public void testAppRouter() throws Exception {
        webTestClient.
            get().uri("/book/authors")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$").value(equalTo("George Orwell"));
    }

    @Test
    public void testPluginRouter() {
        webTestClient
            .get().uri("/admin/plugin")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$").value(equalTo("I am plugin!"));

        pluginManager.stopPlugin("demo-plugin-webflux");

        webTestClient
            .get().uri("/admin/plugin")
            .exchange()
            .expectStatus().isNotFound();

        pluginManager.startPlugin("demo-plugin-webflux");

        webTestClient
            .get().uri("/admin/plugin")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$").value(equalTo("I am plugin!"));
    }

    @Test
    public void testApiDoc() {
        webTestClient
            .get().uri("/v3/api-docs/main")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.paths").value(allOf(
                aMapWithSize(4),
                hasKey("/book/"), // app controller
                hasKey("/book/list"), // app controller
                hasKey("/book/authors"), // app route function
                hasKey("/admin/plugin") // plugin route function
            ));

        webTestClient
            .get().uri("/v3/api-docs/admin")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.paths").value(allOf(
                aMapWithSize(4),
                hasKey("/admin/user"), // plugin controller
                hasKey("/admin/admin"), // plugin controller
                hasKey("/book/authors"), // app route function
                hasKey("/admin/plugin") // plugin route function
            ));

        pluginManager.stopPlugin("demo-plugin-webflux");

        webTestClient
            .get().uri("/v3/api-docs/admin")
            .exchange()
            .expectStatus().isNotFound();

        webTestClient
            .get().uri("/v3/api-docs/main")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.paths").value(allOf(
                aMapWithSize(3),
                hasKey("/book/"), // app controller
                hasKey("/book/list"), // app controller
                hasKey("/book/authors") // app route function
            ));

        pluginManager.startPlugin("demo-plugin-webflux");

        webTestClient
            .get().uri("/v3/api-docs/main")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.paths").value(allOf(
                aMapWithSize(4),
                hasKey("/book/"), // app controller
                hasKey("/book/list"), // app controller
                hasKey("/book/authors"), // app route function
                hasKey("/admin/plugin") // plugin route function
            ));

        webTestClient
            .get().uri("/v3/api-docs/admin")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.paths").value(allOf(
                aMapWithSize(4),
                hasKey("/admin/user"), // plugin controller
                hasKey("/admin/admin"), // plugin controller
                hasKey("/book/authors"), // app route function
                hasKey("/admin/plugin") // plugin route function
            ));
    }
}

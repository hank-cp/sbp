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

import demo.sbp.app.ai.mcp.DemoMcpApp;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SpringBootApplication
public class DemoTestApp extends DemoMcpApp {

    public static void main(String[] args) {
        SpringApplication.run(DemoTestApp.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppContextReady(ApplicationReadyEvent event) {
        McpSyncClient client = McpClient.sync(HttpClientSseClientTransport.builder("http://localhost:8080").build())
            .loggingConsumer(message -> {
                System.out.println(">> Client Logging: " + message);
            }).build();
        client.initialize();
        client.ping();
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        applicationContext.getBeanFactory().registerSingleton("mcpSyncClient", client);
        applicationContext.getBeanFactory().autowireBean(client);
    }
}
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

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoTestApp.class,
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                "org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class McpTest {

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private McpSyncClient client;

    @Test
    @Order(1)
    public void testTools() {
        McpSchema.ListToolsResult toolsList = client.listTools();
        log.debug("Available Tools = {}", toolsList);
        assertThat(toolsList.tools().size(), equalTo(3)); //2+1
        toolsList.tools().forEach(tool -> {
            log.debug("Tool: {}, description: {}, schema: {}", tool.name(), tool.description(), tool.inputSchema());
        });

        McpSchema.CallToolResult weatherForcastResult = client.callTool(new McpSchema.CallToolRequest("getWeatherForecastByLocation",
            Map.of("latitude", "47.6062", "longitude", "-122.3321")));
        log.debug("Weather Forcast result: {}", weatherForcastResult);
        assertThat(weatherForcastResult.content().size(), greaterThan(0));

        McpSchema.CallToolResult helloWorldResult = client.callTool(new McpSchema.CallToolRequest("helloWorld", Map.of()));
        log.debug("Hello world result: {}", helloWorldResult);
        McpSchema.TextContent content = (McpSchema.TextContent)helloWorldResult.content().get(0);
        assertThat(content.text(), equalTo("\"Hi, I am your plugin tools\""));
    }

    @Test
    @Order(2)
    public void testResources() {
        McpSchema.ListResourcesResult resList = client.listResources();
        log.debug("Available Resources = {}", resList);
        assertThat(resList.resources().size(), equalTo(9)); // 8+1
        resList.resources().forEach(res -> {
            log.debug("Resources: {}, description: {}, uri: {}", res.name(), res.description(), res.uri());
        });

        McpSchema.ReadResourceResult resource = client.readResource(new McpSchema.ReadResourceRequest("user-status://alice"));
        log.debug("Weather Forcast result: {}", resource);
        McpSchema.TextResourceContents content = (McpSchema.TextResourceContents) resource.contents().get(0);
        assertThat(content.text(), equalTo("🔴 Busy"));

        McpSchema.ReadResourceResult pluginResource = client.readResource(new McpSchema.ReadResourceRequest("plugin://alice"));
        log.debug("Plugin result: {}", pluginResource);
        content = (McpSchema.TextResourceContents) pluginResource.contents().get(0);
        assertThat(content.text(), equalTo("Hi alice, I am plugin resources."));

        // stop plugin
        pluginManager.stopPlugin("demo-plugin-mcp");
        resList = client.listResources();
        assertThat(resList.resources().size(), equalTo(8));

        // start plugin
        pluginManager.startPlugin("demo-plugin-mcp");
        resList = client.listResources();
        assertThat(resList.resources().size(), equalTo(9));
    }

    @Test
    @Order(3)
    public void testPrompt() {
        McpSchema.ListPromptsResult promptList = client.listPrompts();
        log.debug("Available prompts = {}", promptList);
        assertThat(promptList.prompts().size(), equalTo(7)); // 6+1
        promptList.prompts().forEach(res -> {
            log.debug("Prompt: {}, description: {}, uri: {}", res.name(), res.description(), res.name());
        });

        McpSchema.GetPromptResult promptResult = client.getPrompt(
            new McpSchema.GetPromptRequest("greeting", Map.of("name", "Alice")));
        log.debug("Prompt result: {}", promptResult);
        McpSchema.TextContent content = (McpSchema.TextContent) promptResult.messages().get(0).content();
        assertThat(content.text(), equalTo("Hello, Alice! Welcome to the MCP system."));

        McpSchema.GetPromptResult pluginPromptResult = client.getPrompt(
            new McpSchema.GetPromptRequest("plugin", Map.of("name", "Alice")));
        log.debug("Prompt result: {}", pluginPromptResult);
        content = (McpSchema.TextContent) pluginPromptResult.messages().get(0).content();
        assertThat(content.text(), equalTo("Hello, Alice! This is the prompt from plugin."));
    }

    @Test
    @Order(3)
    public void testCompletion() {
        McpSchema.CompleteResult completion = client.completeCompletion(new McpSchema.CompleteRequest(new McpSchema.ResourceReference("user-status://{username}"),
            new McpSchema.CompleteRequest.CompleteArgument("username", "a")));
        log.debug("Completion result: {}", completion);
        assertThat(completion.completion().values().get(0), equalTo("alex123"));

        completion = client.completeCompletion(new McpSchema.CompleteRequest(new McpSchema.ResourceReference("plugin://{username}"),
            new McpSchema.CompleteRequest.CompleteArgument("username", "a")));
        log.debug("Plugin completion result: {}", completion);
        assertThat(completion.completion().values().get(0), equalTo("a_plugin_1"));
        assertThat(completion.completion().values().size(), equalTo(2));
    }

    // TODO restart plugin on live will make SSE connection unstable.
//    @Test
//    @Order(4)
//    public void testRestartPlugin() {
//        assertThat(client.listTools().tools().size(), equalTo(3));
//        assertThat(client.listResources().resources().size(), equalTo(9));
//        assertThat(client.listPrompts().prompts().size(), equalTo(7));
//
//        // stop plugin
//        pluginManager.stopPlugin("demo-plugin-mcp");
//        sleep(1000);
//        assertThat(client.listTools().tools().size(), equalTo(2));
//        assertThat(client.listResources().resources().size(), equalTo(8));
//        assertThat(client.listPrompts().prompts().size(), equalTo(6));
//
//        // no result from plugin
//        assertThat(exceptionOf(() ->
//            client.callTool(new McpSchema.CallToolRequest("helloWorld", Map.of()))),
//            instanceOf(McpError.class));
//
//        // start plugin
//        pluginManager.startPlugin("demo-plugin-mcp");
//        sleep(1000);
//        assertThat(client.listTools().tools().size(), equalTo(3));
//        assertThat(client.listResources().resources().size(), equalTo(9));
//        assertThat(client.listPrompts().prompts().size(), equalTo(7));
//    }
//
//    private void sleep(long millis) {
//        try {
//            Thread.sleep(millis);
//        } catch (InterruptedException e) {}
//    }
//
//    public interface Runnable {
//        void run() throws Exception;
//    }
//
//    private static Throwable exceptionOf(Runnable runnable) {
//        try {
//            runnable.run();
//            return null;
//        } catch (Throwable t) {
//            return t;
//        }
//    }
}

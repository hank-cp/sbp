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
package demo.sbp.plugin_mcp;

import com.logaritex.mcp.spring.SpringAiMcpAnnotationProvider;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.laxture.spring.util.ApplicationContextProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SpringBootApplication
public class McpPluginStarter {

    public static void main(String[] args) {
        SpringApplication.run(McpPluginStarter.class, args);
    }

    @Bean
    public ApplicationContextAware multiApplicationContextProviderRegister() {
        return ApplicationContextProvider::registerApplicationContext;
    }

    @Bean
    public ToolCallbackProvider weatherTools(PluginToolService toolService) {
        return MethodToolCallbackProvider.builder().toolObjects(toolService).build();
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> resourceSpecs(PluginResourceService pluginResourceProvider) {
        return SpringAiMcpAnnotationProvider.createSyncResourceSpecifications(List.of(pluginResourceProvider));
    }

    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> promptSpecs(PluginPromptService promptProvider) {
        return SpringAiMcpAnnotationProvider.createSyncPromptSpecifications(List.of(promptProvider));
    }

    @Bean
    public List<McpServerFeatures.SyncCompletionSpecification> completionSpecs(PluginCompletionService autocompleteProvider) {
        return SpringAiMcpAnnotationProvider.createSyncCompleteSpecifications(List.of(autocompleteProvider));
    }

}

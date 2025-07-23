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
package org.laxture.sbp.spring.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.IPluginConfigurer;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Plugin configurer for Spring AI mcp.
 */
@Slf4j
public class SbpSpringMcpConfigurer implements IPluginConfigurer {

    private IMcpServiceConfigAdapter<?, ?, ?, ?> mcpServiceConfigAdapter;
    private final Set<String> toolNames = new HashSet<>();
    private final Set<String> resourceNames = new HashSet<>();
    private final Set<String> promptNames = new HashSet<>();
    private final Set<McpSchema.CompleteReference> completionKeys = new HashSet<>();

    @Override
    public String[] excludeConfigurations() {
        return new String[] {
            "org.springframework.ai.mcp.server.autoconfigure.McpServerAutoConfiguration",
            "org.springframework.ai.mcp.server.autoconfigure.McpWebMvcServerAutoConfiguration",
            "org.springframework.ai.mcp.server.autoconfigure.McpWebFluxServerAutoConfiguration",
        };
    }

    @Override
    public void onStart(SpringBootPlugin plugin) {
        ApplicationContext mainAppCtx = plugin.getMainApplicationContext();
        ApplicationContext pluginAppCtx = plugin.getApplicationContext();
        mcpServiceConfigAdapter = "SYNC".equals(mainAppCtx.getEnvironment().getProperty("spring.ai.mcp.server.type"))
            ? new SyncMcpConfigAdapter(mainAppCtx) : new AsyncMcpConfigAdapter(mainAppCtx);
        // register tools from toolSpec
        toolNames.addAll(mcpServiceConfigAdapter.registerTools(pluginAppCtx));
        log.info("Registered {} tools from Plugin.", toolNames.size());

        resourceNames.addAll(mcpServiceConfigAdapter.registerResources(pluginAppCtx));
        log.info("Registered {} resources from Plugin.", resourceNames.size());

        promptNames.addAll(mcpServiceConfigAdapter.registerPrompts(pluginAppCtx));
        log.info("Registered {} prompts from Plugin.", promptNames.size());

        completionKeys.addAll(mcpServiceConfigAdapter.registerCompletions(pluginAppCtx));
        log.info("Registered {} completions from Plugin.", completionKeys.size());
    }

    @Override
    public void onStop(SpringBootPlugin plugin) {
        this.unregisterMcpServices();
    }

    @Override
    public void releaseLeaveOverResource(PluginWrapper plugin, GenericApplicationContext mainAppCtx) {
        this.unregisterMcpServices();
    }

    private void unregisterMcpServices() {
        toolNames.forEach(toolName -> mcpServiceConfigAdapter.unregisterTool(toolName));
        resourceNames.forEach(resName -> mcpServiceConfigAdapter.unregisterResource(resName));
        promptNames.forEach(promptName -> mcpServiceConfigAdapter.unregisterPrompt(promptName));
        completionKeys.forEach(completionKey -> mcpServiceConfigAdapter.unregisterCompletion(completionKey));
    }
}

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

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.laxture.sbp.util.BeanUtil;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import java.util.*;
import java.util.stream.Collectors;

class AsyncMcpConfigAdapter implements IMcpServiceConfigAdapter<
    McpServerFeatures.AsyncToolSpecification,
    McpServerFeatures.AsyncResourceSpecification,
    McpServerFeatures.AsyncPromptSpecification,
    McpServerFeatures.AsyncCompletionSpecification> {

    private McpAsyncServer mcpAsyncServer;

    AsyncMcpConfigAdapter(ApplicationContext mainAppCtx) {
        try {
            mcpAsyncServer = mainAppCtx.getBean(McpAsyncServer.class);
//            mcpServerProperties = mainAppCtx.getBean(McpServerProperties.class);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("Spring AI mcp services is not enabled");
        }
    }

    @Override
    public Set<String> registerTools(ApplicationContext pluginAppCtx) {
        Set<String> toolNames = new HashSet<>();

        pluginAppCtx.getBeanProvider(McpServerFeatures.AsyncToolSpecification.class).stream().forEachOrdered(toolSpec -> {
            mcpAsyncServer.addTool(toolSpec);
            toolNames.add(toolSpec.tool().name());
        });

        pluginAppCtx.getBeanProvider(ToolCallbackProvider.class).stream().forEachOrdered(provider -> {
            Arrays.stream(provider.getToolCallbacks()).forEachOrdered(toolCallback -> {
                String toolName = toolCallback.getToolDefinition().name();
//                MimeType mimeType = mcpServerProperties.getToolResponseMimeType().containsKey(toolName)
//                    ? MimeType.valueOf(mcpServerProperties.getToolResponseMimeType().get(toolName)) : null;
                McpServerFeatures.AsyncToolSpecification toolSpec = McpToolUtils.toAsyncToolSpecification(toolCallback, null);
                mcpAsyncServer.addTool(toolSpec);
                toolNames.add(toolName);
            });
        });

        return toolNames;
    }

    @Override
    public Set<String> registerResources(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.AsyncResourceSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.AsyncResourceSpecification>) beans)
            .flatMap(Collection::stream).peek(resSpec -> {
                mcpAsyncServer.addResource(resSpec);
            }).map(resSpec -> resSpec.resource().uri())
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> registerPrompts(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.AsyncPromptSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.AsyncPromptSpecification>) beans)
            .flatMap(Collection::stream).peek(promptSpec -> {
                mcpAsyncServer.addPrompt(promptSpec);
            }).map(promptSpec -> promptSpec.prompt().name())
            .collect(Collectors.toSet());
    }

    @Override
    public Set<McpSchema.CompleteReference> registerCompletions(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.AsyncCompletionSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.AsyncCompletionSpecification>) beans)
            .flatMap(Collection::stream).peek(completionSpec -> {
                Map<McpSchema.CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions = BeanUtil.getFieldValue(mcpAsyncServer, "completions");
                completions.put(completionSpec.referenceKey(), completionSpec);
            }).map(McpServerFeatures.AsyncCompletionSpecification::referenceKey)
            .collect(Collectors.toSet());
    }

    @Override
    public void unregisterTool(String toolName) {
        mcpAsyncServer.removeTool(toolName);
    }

    @Override
    public void unregisterResource(String resourceName) {
        mcpAsyncServer.removeResource(resourceName);
    }

    @Override
    public void unregisterPrompt(String promptName) {
        mcpAsyncServer.removePrompt(promptName);
    }

    @Override
    public void unregisterCompletion(McpSchema.CompleteReference completionKey) {
        Map<McpSchema.CompleteReference, McpServerFeatures.AsyncCompletionSpecification> completions = BeanUtil.getFieldValue(mcpAsyncServer, "completions");
        completions.remove(completionKey);
    }
}

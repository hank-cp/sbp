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

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import org.laxture.sbp.util.BeanUtil;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

class SyncMcpConfigAdapter implements IMcpServiceConfigAdapter<
    McpServerFeatures.SyncToolSpecification,
    McpServerFeatures.SyncResourceSpecification,
    McpServerFeatures.SyncPromptSpecification,
    McpServerFeatures.SyncCompletionSpecification> {

    private McpSyncServer mcpSyncServer;
//    private McpServerProperties mcpServerProperties;

    SyncMcpConfigAdapter(ApplicationContext mainAppCtx) {
        try {
            mcpSyncServer = mainAppCtx.getBean(McpSyncServer.class);
//            mcpServerProperties = mainAppCtx.getBean(McpServerProperties.class);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new IllegalStateException("Spring AI mcp services is not enabled");
        }
    }

    @Override
    public Set<String> registerTools(ApplicationContext pluginAppCtx) {
        Set<String> toolNames = new HashSet<>();

        pluginAppCtx.getBeanProvider(McpServerFeatures.SyncToolSpecification.class).stream().forEachOrdered(toolSpec -> {
            mcpSyncServer.addTool(toolSpec);
            toolNames.add(toolSpec.tool().name());
        });

        pluginAppCtx.getBeanProvider(ToolCallbackProvider.class).stream().forEachOrdered(provider -> {
            Arrays.stream(provider.getToolCallbacks()).forEachOrdered(toolCallback -> {
                String toolName = toolCallback.getToolDefinition().name();
//                MimeType mimeType = mcpServerProperties.getToolResponseMimeType().containsKey(toolName)
//                    ? MimeType.valueOf(mcpServerProperties.getToolResponseMimeType().get(toolName)) : null;
                McpServerFeatures.SyncToolSpecification toolSpec = McpToolUtils.toSyncToolSpecification(toolCallback, null);
                mcpSyncServer.addTool(toolSpec);
                toolNames.add(toolName);
            });
        });

        return toolNames;
    }

    @Override
    public Set<String> registerResources(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.SyncResourceSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.SyncResourceSpecification>) beans)
            .flatMap(Collection::stream).peek(resSpec -> {
                mcpSyncServer.addResource(resSpec);
            }).map(resSpec -> resSpec.resource().uri())
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> registerPrompts(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.SyncPromptSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.SyncPromptSpecification>) beans)
            .flatMap(Collection::stream).peek(promptSpec -> {
                mcpSyncServer.addPrompt(promptSpec);
            }).map(promptSpec -> promptSpec.prompt().name())
            .collect(Collectors.toSet());
    }

    @Override
    public Set<McpSchema.CompleteReference> registerCompletions(ApplicationContext pluginAppCtx) {
        return pluginAppCtx.getBeanProvider(ResolvableType.forClassWithGenerics(List.class, McpServerFeatures.SyncCompletionSpecification.class))
            .stream().map(beans -> (List<McpServerFeatures.SyncCompletionSpecification>) beans)
            .flatMap(Collection::stream).peek(completionSpec -> {
                Map completions = BeanUtil.getFieldValue(mcpSyncServer, "asyncServer.completions");
                McpServerFeatures.AsyncCompletionSpecification asyncSpec = new McpServerFeatures.AsyncCompletionSpecification(completionSpec.referenceKey(),
                    (exchange, request) -> Mono.fromCallable(
                            () -> completionSpec.completionHandler().apply(new McpSyncServerExchange(exchange), request))
                        .subscribeOn(Schedulers.boundedElastic()));
                completions.put(completionSpec.referenceKey(), asyncSpec);
            }).map(McpServerFeatures.SyncCompletionSpecification::referenceKey)
            .collect(Collectors.toSet());
    }

    @Override
    public void unregisterTool(String toolName) {
        mcpSyncServer.removeTool(toolName);
    }

    @Override
    public void unregisterResource(String resourceName) {
        mcpSyncServer.removeResource(resourceName);
    }

    @Override
    public void unregisterPrompt(String promptName) {
        mcpSyncServer.removePrompt(promptName);
    }

    @Override
    public void unregisterCompletion(McpSchema.CompleteReference completionKey) {
        Map<McpSchema.CompleteReference, McpServerFeatures.SyncCompletionSpecification> completions = BeanUtil.getFieldValue(mcpSyncServer, "asyncServer.completions");
        completions.remove(completionKey);
    }
}
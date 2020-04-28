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
package org.laxture.sbp.spring.boot;

import org.laxture.sbp.SpringBootPluginManager;
import org.laxture.sbp.spring.boot.model.PluginInfo;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
public class PluginManagerController {

    @Autowired
    private SpringBootPluginManager pluginManager;

    @GetMapping(value = "${spring.sbp.controller.base-path:/sbp}/list")
    public List<PluginInfo> list() {
        List<PluginWrapper> loadedPlugins = pluginManager.getPlugins();

        List<PluginInfo> plugins = loadedPlugins.stream().map(pluginWrapper -> {
                    PluginDescriptor descriptor = pluginWrapper.getDescriptor();
                    PluginDescriptor latestDescriptor = null;
                    try {
                        latestDescriptor = pluginManager.getPluginDescriptorFinder()
                                .find(pluginWrapper.getPluginPath());
                    } catch (PluginRuntimeException ignored) {}
                    String newVersion = null;
                    if (latestDescriptor != null && !descriptor.getVersion().equals(latestDescriptor.getVersion())) {
                        newVersion = latestDescriptor.getVersion();
                    }

                    return PluginInfo.build(descriptor,
                            pluginWrapper.getPluginState(), newVersion,
                            latestDescriptor == null);
                }).collect(Collectors.toList());

        List<Path> pluginPaths = pluginManager.getPluginRepository().getPluginPaths();
        plugins.addAll(pluginPaths.stream().filter(path ->
            loadedPlugins.stream().noneMatch(plugin -> plugin.getPluginPath().equals(path))
        ).map(path -> {
            PluginDescriptor descriptor = pluginManager
                    .getPluginDescriptorFinder().find(path);
            return PluginInfo.build(descriptor, null, null, false);
        }).collect(Collectors.toList()));

        return plugins;
    }

    @PostMapping(value = "${spring.sbp.controller.base-path:/sbp}/start/{pluginId}")
    public int start(@PathVariable String pluginId) {
        pluginManager.startPlugin(pluginId);
        return 0;
    }

    @PostMapping(value = "${spring.sbp.controller.base-path:/sbp}/stop/{pluginId}")
    public int stop(@PathVariable String pluginId) {
        pluginManager.stopPlugin(pluginId);
        return 0;
    }

    @PostMapping(value = "${spring.sbp.controller.base-path:/sbp}/restart/{pluginId}")
    public int restart(@PathVariable String pluginId) {
        PluginWrapper plugin = pluginManager.getPlugin(pluginId);
        if (plugin == null) return -1;
        pluginManager.unloadPlugin(pluginId);
        String newPluginId = pluginManager.loadPlugin(plugin.getPluginPath());
        pluginManager.startPlugin(newPluginId);
        return 0;
    }

    @PostMapping(value = "${spring.sbp.controller.base-path:/sbp}/restart-all")
    public int restartAll() {
        List<String> startedPluginIds = new ArrayList<>();
        List<PluginWrapper> loadedPlugins = pluginManager.getPlugins();
        loadedPlugins.forEach(plugin -> {
            if (plugin.getPluginState() == PluginState.STARTED) {
                startedPluginIds.add(plugin.getPluginId());
            }
            pluginManager.unloadPlugin(plugin.getPluginId());
        });
        pluginManager.loadPlugins();
        startedPluginIds.forEach(pluginId -> {
            // restart started plugin
            if (pluginManager.getPlugin(pluginId) != null) {
                pluginManager.startPlugin(pluginId);
            }
        });
        return 0;
    }

}

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

import org.pf4j.PluginStatusProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PropertyPluginStatusProvider implements PluginStatusProvider {

    private List<String> enabledPlugins;
    private List<String> disabledPlugins;

    public PropertyPluginStatusProvider(SbpProperties sbpProperties) {
        this.enabledPlugins = sbpProperties.getEnabledPlugins() != null
                ? Arrays.asList(sbpProperties.getEnabledPlugins()) : new ArrayList<>();
        this.disabledPlugins = sbpProperties.getDisabledPlugins() != null
                ? Arrays.asList(sbpProperties.getDisabledPlugins()) : new ArrayList<>();
    }

    public static boolean isPropertySet(SbpProperties sbpProperties) {
        return sbpProperties.getEnabledPlugins() != null && sbpProperties.getEnabledPlugins().length > 0
                || sbpProperties.getDisabledPlugins() != null && sbpProperties.getDisabledPlugins().length > 0;
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {
        if (disabledPlugins.contains(pluginId)) return true;
        return !enabledPlugins.isEmpty() && !enabledPlugins.contains(pluginId);
    }

    @Override
    public void disablePlugin(String pluginId) {
        if (isPluginDisabled(pluginId)) return;
        disabledPlugins.add(pluginId);
        enabledPlugins.remove(pluginId);
    }

    @Override
    public void enablePlugin(String pluginId) {
        if (!isPluginDisabled(pluginId)) return;
        enabledPlugins.add(pluginId);
        disabledPlugins.remove(pluginId);
    }
}

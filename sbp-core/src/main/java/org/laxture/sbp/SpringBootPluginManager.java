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
package org.laxture.sbp;

import org.laxture.sbp.internal.SpringExtensionFactory;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRepository;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * PluginManager to hold the main ApplicationContext
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringBootPluginManager extends DefaultPluginManager
        implements ApplicationContextAware {

    private boolean mainApplicationStarted;
    private ApplicationContext mainApplicationContext;
    public Map<String, Object> presetProperties = new HashMap<>();
    private boolean autoStartPlugin = true;
    private String[] profiles;
    private PluginRepository pluginRepository;

    public SpringBootPluginManager() {
        super();
    }

    public SpringBootPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SpringExtensionFactory(this);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.mainApplicationContext = applicationContext;
    }

    @Override
    public PluginDescriptorFinder getPluginDescriptorFinder() {
        return super.getPluginDescriptorFinder();
    }

    @Override
    protected PluginRepository createPluginRepository() {
        this.pluginRepository = super.createPluginRepository();
        return this.pluginRepository;
    }

    public PluginRepository getPluginRepository() {
        return pluginRepository;
    }

    public void setAutoStartPlugin(boolean autoStartPlugin) {
        this.autoStartPlugin = autoStartPlugin;
    }

    public void setMainApplicationStarted(boolean mainApplicationStarted) {
        this.mainApplicationStarted = mainApplicationStarted;
    }

    public void setProfiles(String[] profiles) {
        this.profiles = profiles;
    }

    public String[] getProfiles() {
        return profiles;
    }

    public void presetProperties(Map<String, Object> presetProperties) {
        this.presetProperties.putAll(presetProperties);
    }

    public void presetProperties(String name, Object value) {
        this.presetProperties.put(name, value);
    }

    public Map<String, Object> getPresetProperties() {
        return presetProperties;
    }

    public ApplicationContext getMainApplicationContext() {
        return mainApplicationContext;
    }

    public boolean isMainApplicationStarted() {
        return mainApplicationStarted;
    }

    /**
     * This method load, start plugins and inject extensions in Spring
     */
    @PostConstruct
    public void init() {
        loadPlugins();
        if (autoStartPlugin) startPlugins();
    }

}

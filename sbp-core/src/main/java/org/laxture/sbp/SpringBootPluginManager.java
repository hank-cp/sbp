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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.laxture.sbp.internal.SpringExtensionFactory;
import org.laxture.sbp.spring.boot.PluginStartingError;
import org.laxture.sbp.spring.boot.SbpPluginStateChangedEvent;
import org.pf4j.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PluginManager to hold the main ApplicationContext
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Slf4j
public class SpringBootPluginManager extends DefaultPluginManager
        implements ApplicationContextAware, InitializingBean {

    private boolean mainApplicationStarted;
    private GenericApplicationContext mainApplicationContext;
    public Map<String, Object> presetProperties = new HashMap<>();
    private boolean autoStartPlugin = true;
    private String[] profiles;
    private PluginRepository pluginRepository;
    private final Map<String, PluginStartingError> startingErrors = new HashMap<>();

    @Getter
    private ReentrantLock loadingLock = new ReentrantLock();

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
        this.mainApplicationContext = (GenericApplicationContext) applicationContext;
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

    public boolean isAutoStartPlugin() {
        return autoStartPlugin;
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
    @Override
    public void afterPropertiesSet() {
        if (this.autoStartPlugin) loadingLock.lock();
        loadPlugins();
   }

    public PluginStartingError getPluginStartingError(String pluginId) {
        return startingErrors.get(pluginId);
    }

    //*************************************************************************
    // Plugin State Manipulation
    //*************************************************************************

    public boolean isLoading() {
        return loadingLock.isLocked();
    }

    public void releaseLoadingLock() {
        loadingLock.unlock();
    }

    private void doStartPlugins() {
        loadingLock.lock();
        long ts = System.currentTimeMillis();

        for (PluginWrapper pluginWrapper : resolvedPlugins) {
            PluginState pluginState = pluginWrapper.getPluginState();
            if ((PluginState.DISABLED != pluginState) && (PluginState.STARTED != pluginState)) {
                if (pluginWrapper.getPlugin() == null) {
                    loadingLock.unlock();
                    throw new IllegalArgumentException("pluginId " + pluginWrapper.getPluginId() + " doesn't existed.");
                }
                try {
                    pluginWrapper.getPlugin().start();
                    pluginWrapper.setPluginState(PluginState.STARTED);
                    startedPlugins.add(pluginWrapper);

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    startingErrors.put(pluginWrapper.getPluginId(), PluginStartingError.of(
                            pluginWrapper.getPluginId(), e.getMessage(), e.toString()));
                    SpringBootPlugin.releaseLegacyResources(pluginWrapper, mainApplicationContext);
                }
            }
        }

        log.info("[SBP] {} plugins are started in {}ms. {} failed", getPlugins(PluginState.STARTED).size(),
                System.currentTimeMillis() - ts, startingErrors.size());
    }

    private void doStopPlugins() {
        startingErrors.clear();
        // stop started plugins in reverse order
        Collections.reverse(startedPlugins);
        Iterator<PluginWrapper> itr = startedPlugins.iterator();
        while (itr.hasNext()) {
            PluginWrapper pluginWrapper = itr.next();
            PluginState pluginState = pluginWrapper.getPluginState();
            if (PluginState.STARTED == pluginState) {
                try {
                    log.info("Stop plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    pluginWrapper.getPlugin().stop();
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();

                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginRuntimeException e) {
                    log.error(e.getMessage(), e);
                    startingErrors.put(pluginWrapper.getPluginId(), PluginStartingError.of(
                            pluginWrapper.getPluginId(), e.getMessage(), e.toString()));
                }
            }
        }
    }

    private PluginState doStartPlugin(String pluginId, boolean sendEvent) {
        PluginWrapper plugin = getPlugin(pluginId);
        PluginState previousState = plugin.getPluginState();
        try {
            PluginState pluginState = super.startPlugin(pluginId);
            if (sendEvent && previousState != pluginState) {
                mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
            }
            return pluginState;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            startingErrors.put(plugin.getPluginId(), PluginStartingError.of(
                    plugin.getPluginId(), e.getMessage(), e.toString()));
            SpringBootPlugin.releaseLegacyResources(plugin, mainApplicationContext);
        }
        return plugin.getPluginState();
    }

    private PluginState doStopPlugin(String pluginId, boolean sendEvent) {
        PluginWrapper plugin = getPlugin(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("pluginId " + pluginId + " doesn't existed.");
        }
        PluginState previousState = plugin.getPluginState();
        try {
            PluginState pluginState = super.stopPlugin(pluginId);
            if (sendEvent && previousState != pluginState) {
                mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
            }
            return pluginState;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            startingErrors.put(plugin.getPluginId(), PluginStartingError.of(
                    plugin.getPluginId(), e.getMessage(), e.toString()));
        }
        return plugin.getPluginState();
    }

    @Override
    public void startPlugins() {
        try {
            doStartPlugins();
            mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
        } finally {
            loadingLock.unlock();
        }
    }

    @Override
    public PluginState startPlugin(String pluginId) {
        try {
            loadingLock.lock();
            return doStartPlugin(pluginId, true);
        } finally {
            loadingLock.unlock();
        }
    }

    @Override
    public void stopPlugins() {
        try {
            loadingLock.lock();
            doStopPlugins();
            mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
        } finally {
            loadingLock.unlock();
        }
    }

    @Override
    public PluginState stopPlugin(String pluginId) {
        try {
            loadingLock.lock();
            return doStopPlugin(pluginId, true);
        } finally {
            loadingLock.unlock();
        }
    }

    public void restartPlugins() {
        try {
            loadingLock.lock();
            doStopPlugins();
            doStartPlugins();
        } finally {
            loadingLock.unlock();
        }
    }

    public PluginState restartPlugin(String pluginId) {
        try {
            loadingLock.lock();
            PluginState pluginState = doStopPlugin(pluginId, false);
            if (pluginState != PluginState.STARTED) doStartPlugin(pluginId, false);
            doStartPlugin(pluginId, false);
            mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
            return pluginState;
        } finally {
            loadingLock.unlock();
        }
    }

    public void reloadPlugins(boolean restartStartedOnly) {
        try {
            loadingLock.lock();
            doStopPlugins();
            List<String> startedPluginIds = new ArrayList<>();
            getPlugins().forEach(plugin -> {
                if (plugin.getPluginState() == PluginState.STARTED) {
                    startedPluginIds.add(plugin.getPluginId());
                }
                unloadPlugin(plugin.getPluginId());
            });
            loadPlugins();
            if (restartStartedOnly) {
                startedPluginIds.forEach(pluginId -> {
                    // restart started plugin
                    if (getPlugin(pluginId) != null) {
                        doStartPlugin(pluginId, false);
                    }
                });
                mainApplicationContext.publishEvent(new SbpPluginStateChangedEvent(mainApplicationContext));
            } else {
                startPlugins();
            }
        } finally {
            loadingLock.unlock();
        }
    }

    public PluginState reloadPlugins(String pluginId) {
        try {
            loadingLock.lock();
            PluginWrapper plugin = getPlugin(pluginId);
            doStopPlugin(pluginId, false);
            unloadPlugin(pluginId, false);
            try {
                loadPlugin(plugin.getPluginPath());
            } catch (Exception ex) {
                return null;
            }

            return doStartPlugin(pluginId, true);
        } finally {
            loadingLock.unlock();
        }
    }

}

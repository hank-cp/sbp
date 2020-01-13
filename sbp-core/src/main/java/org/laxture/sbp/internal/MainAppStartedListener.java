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
package org.laxture.sbp.internal;

import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.SpringBootPluginManager;
import org.laxture.sbp.spring.boot.SbpMainAppStartedEvent;
import org.pf4j.PluginState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Component
public class MainAppStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private SpringBootPluginManager pluginManager;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        pluginManager.getPlugins(PluginState.STARTED).forEach(pluginWrapper -> {
            SpringBootPlugin springBootPlugin = (SpringBootPlugin) pluginWrapper.getPlugin();
            ApplicationContext pluginAppCtx = springBootPlugin.getApplicationContext();
            pluginAppCtx.publishEvent(new SbpMainAppStartedEvent(applicationContext));
        });
        pluginManager.setMainApplicationStarted(true);
    }

}

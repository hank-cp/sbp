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

import org.laxture.sbp.SpringBootPlugin;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public interface IPluginConfigurer {

    default String[] excludeConfigurations() {
        return new String[] {};
    }


    /**
     * Hook of creating plugin ApplicationContext. Could import beans from main ApplicationContext
     * or register extension to main ApplicationContext.
     *
     * Note that plugin ApplicationContext is not yet ready in this hook, use <@link #afterBootstrap> instead.
     */
    default void onBootstrap(SpringBootstrap bootstrap,
                             GenericApplicationContext pluginApplicationContext) {
        // default do nothing
    }

    /**
     * Hook of finishing creating plugin ApplicationContext. If the extension is relied on
     * plugin beans, it should be done in this hook.
     */
    default void afterBootstrap(SpringBootstrap bootstrap,
                                GenericApplicationContext pluginApplicationContext) {
        // default do nothing
    }

    default void onStart(SpringBootPlugin plugin) {
        // default do nothing
    }

    default void onStop(SpringBootPlugin plugin) {
        // default do nothing
    }

    /**
     * Release plugin leave-over resources in main ApplicationContext.
     */
    default void releaseLeaveOverResource(PluginWrapper plugin,
                                          GenericApplicationContext mainAppCtx) {
        // default do nothing
    }

}

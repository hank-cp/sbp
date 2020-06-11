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
package demo.sbp.app;

import org.laxture.sbp.internal.SpringBootPluginClassLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.pf4j.util.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class CustomPluginLoader implements PluginLoader {

    protected PluginManager pluginManager;

    public CustomPluginLoader(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && FileUtils.isJarFile(pluginPath);
    }

    @Override
    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
        PluginClassLoader pluginClassLoader = new SpringBootPluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader());
        pluginClassLoader.addFile(pluginPath.toFile());
        // TODO add jarFile in jar
        return pluginClassLoader;
    }
}

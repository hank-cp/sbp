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

import lombok.NonNull;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringBootPluginClassLoader extends PluginClassLoader {

    private static final Logger log = LoggerFactory.getLogger(SpringBootPluginClassLoader.class);

    private List<String> pluginFirstClasses;

    private List<String> pluginOnlyResources;

    public SpringBootPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        // load class from parent first to avoid same class loaded by different classLoader,
        // so Spring could autowired bean by type correctly.
        super(pluginManager, pluginDescriptor, parent, true);
    }

    public void setPluginFirstClasses(@NonNull List<String> pluginFirstClasses) {
        this.pluginFirstClasses = pluginFirstClasses.stream()
                .map(pluginFirstClass -> pluginFirstClass
                        .replaceAll(".", "[$0]")
                        .replace("[*]", ".*?")
                        .replace("[?]", ".?"))
                .collect(Collectors.toList());
    }

    public void setPluginOnlyResources(@NonNull List<String> pluginOnlyResources) {
        this.pluginOnlyResources = pluginOnlyResources.stream()
                .map(pluginFirstClass -> pluginFirstClass
                        .replaceAll(".", "[$0]")
                        .replace("[*]", ".*?")
                        .replace("[?]", ".?"))
                .collect(Collectors.toList());
    }

    @Override
    public URL getResource(String name) {
        if (name.endsWith(".class")) return super.getResource(name);

        // load plain resource from local classpath
        URL url = findResource(name);
        if (url != null) {
            log.trace("Found resource '{}' in plugin classpath", name);
            return url;
        }
        log.trace("Couldn't find resource '{}' in plugin classpath. Delegating to parent", name);
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return isPluginOnlyResources(name) ? findResources(name) : super.getResources(name);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            // if specified, try to load from plugin classpath first
            if (isPluginFirstClass(className)) {
                try {
                    return loadClassFromPlugin(className);
                } catch (ClassNotFoundException ignored) {}
            }
            // not found, load from parent
            return super.loadClass(className);
        } catch (ClassNotFoundException ignored) {}

        // try again in in dependencies classpath
        return loadClassFromDependencies(className);
    }

    private boolean isPluginFirstClass(String name) {
        if (pluginFirstClasses == null || pluginFirstClasses.size() <= 0) return false;
        for (String pluginFirstClass : pluginFirstClasses) {
            if (name.matches(pluginFirstClass)) return true;
        }
        return false;
    }

    private boolean isPluginOnlyResources(String name) {
        if (pluginOnlyResources == null || pluginOnlyResources.size() <= 0) return false;
        for (String pluginOnlyResource : pluginOnlyResources) {
            if (name.matches(pluginOnlyResource)) return true;
        }
        return false;
    }

    private Class<?> loadClassFromPlugin(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
            log.trace("Received request to load class '{}'", className);

            // second check whether it's already been loaded
            Class<?> loadedClass = findLoadedClass(className);
            if (loadedClass != null) {
                log.trace("Found loaded class '{}'", className);
                return loadedClass;
            }

            // nope, try to load locally
            try {
                loadedClass = findClass(className);
                log.trace("Found class '{}' in plugin classpath", className);
                return loadedClass;
            } catch (ClassNotFoundException ignored) {}

            // try next step
            return loadClassFromDependencies(className);
        }
    }

    private Class<?> loadClassFromDependencies(String className) throws ClassNotFoundException {
        try {
            Method loadClassFromDependenciesMethod = ReflectionUtils.findMethod(getClass().getSuperclass(),
                    "loadClassFromDependencies", String.class);
            loadClassFromDependenciesMethod.setAccessible(true);
            Class<?> loadedClass = (Class<?>) loadClassFromDependenciesMethod.invoke(this, className);
            if (loadedClass != null) {
                log.trace("Found class '{}' in dependencies", className);
                return loadedClass;
            }
        } catch (Exception ex) {
            throw new ClassNotFoundException(className);
        }
        throw new ClassNotFoundException(className);
    }

}

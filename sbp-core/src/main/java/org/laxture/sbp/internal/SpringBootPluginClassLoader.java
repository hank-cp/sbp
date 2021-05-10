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
import org.pf4j.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
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
    private PluginManager pluginManager;
    private PluginDescriptor pluginDescriptor;

    public SpringBootPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        // load class from parent first to avoid same class loaded by different classLoader,
        // so Spring could autowired bean by type correctly.
        super(pluginManager, pluginDescriptor, parent, ClassLoadingStrategy.APD);
        this.pluginManager = pluginManager;
        this.pluginDescriptor = pluginDescriptor;
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

    /**
     * load class: application ~~ plugin<br>
     * load ordinary files: plugin ~~ application
     */
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
        if (name.endsWith(".class")) return super.getResources(name);
        return isPluginOnlyResources(name) ? findResources(name) : super.getResources(name);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        // if specified, try to load from plugin classpath first
        if (isPluginFirstClass(className)) {
            try {
                return loadClassFromPlugin(className);
            } catch (ClassNotFoundException ignored) {}
        }
        // not found, load from parent
        return super.loadClass(className);
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

    protected Class<?> getLoadedClass(String className) {
        return findLoadedClass(className);
    }

    protected Class<?> loadClassFromDependencies(String className) {
        log.trace("Search in dependencies for class '{}'", className);
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
            ClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());

            // If the dependency is marked as optional, its class loader might not be available.
            if (classLoader == null && dependency.isOptional()) {
                continue;
            }

            try {
                if (classLoader instanceof SpringBootPluginClassLoader) {
                    // OPTIMIZATION: load classes from loadedClasses only to speed up class loading
                    Class<?> clazz = ((SpringBootPluginClassLoader) classLoader).getLoadedClass(className);
                    if (clazz != null) return clazz;
                    // continue to find class from dependent plugin recursively
                    clazz = ((SpringBootPluginClassLoader) classLoader).loadClassFromDependencies(className);
                    if (clazz != null) return clazz;
                } else {
                    return classLoader.loadClass(className);
                }
            } catch (ClassNotFoundException e) {
                // try next dependency
            }
        }

        return null;
    }

    @Override
    protected URL findResourceFromDependencies(String name) {
        if (!name.endsWith(".class")) return null; // do not load ordinary resource from dependencies
        return super.findResourceFromDependencies(name);
    }

    @Override
    protected Collection<URL> findResourcesFromDependencies(String name) throws IOException {
        if (!name.endsWith(".class")) return Collections.emptyList(); // do not load ordinary resource from dependencies
        return super.findResourcesFromDependencies(name);
    }
}

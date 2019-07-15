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
package org.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class SpringBootPluginClassLoader extends PluginClassLoader {

    private static final Logger log = LoggerFactory.getLogger(SpringBootPluginClassLoader.class);

    public SpringBootPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        // load class from parent first to avoid same class loaded by different classLoader,
        // so Spring could autowired bean by type correctly.
        super(pluginManager, pluginDescriptor, parent, true);
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
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        try {
            return super.loadClass(className);
        } catch (ClassNotFoundException e) {
            // try again in in dependencies classpath
            try {
                Method loadClassFromDependenciesMethod = ReflectionUtils.findMethod(getClass(),
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
        }

        throw new ClassNotFoundException(className);
    }
}

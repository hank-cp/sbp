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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class PluginPersistenceManagedTypes implements PersistenceManagedTypes {

    private final List<String> managedClassNames = Collections.synchronizedList(new ArrayList<>());

    private final List<String> managedPackages = Collections.synchronizedList(new ArrayList<>());

    public void registerPackage(ApplicationContext applicationContext) {
        String[] packagesToScan = getPackagesToScan(applicationContext);
        this.registerPackage(applicationContext, packagesToScan);
    }

    public void registerPackage(ApplicationContext applicationContext, String[] packagesToScan) {
        PersistenceManagedTypes packageTypes = new PersistenceManagedTypesScanner(applicationContext).scan(packagesToScan);
        // TODO remove existed classes first, maybe from failed staging plugin
        this.managedClassNames.addAll(packageTypes.getManagedClassNames());
        this.managedPackages.addAll(packageTypes.getManagedPackages());
    }

    public void unregisterPackage(ApplicationContext applicationContext) {
        String[] packagesToScan = getPackagesToScan(applicationContext);
        this.unregisterPackage(applicationContext, packagesToScan);
    }

    public void unregisterPackage(ApplicationContext applicationContext, String[] packagesToScan) {
        PersistenceManagedTypes packageTypes = new PersistenceManagedTypesScanner(applicationContext).scan(packagesToScan);
        this.managedClassNames.removeAll(packageTypes.getManagedClassNames());
        this.managedPackages.removeAll(packageTypes.getManagedPackages());
    }

    private static String[] getPackagesToScan(BeanFactory beanFactory) {
        List<String> packages = EntityScanPackages.get(beanFactory).getPackageNames();
        if (packages.isEmpty() && AutoConfigurationPackages.has(beanFactory)) {
            packages = AutoConfigurationPackages.get(beanFactory);
        }
        return StringUtils.toStringArray(packages);
    }

    @Override
    public List<String> getManagedClassNames() {
        return this.managedClassNames;
    }

    @Override
    public List<String> getManagedPackages() {
        return this.managedPackages;
    }

    @Override
    @Nullable
    public URL getPersistenceUnitRootUrl() {
        return null;
    }
}

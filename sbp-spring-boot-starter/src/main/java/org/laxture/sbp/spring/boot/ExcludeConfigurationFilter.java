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

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;

import java.util.Arrays;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ExcludeConfigurationFilter implements AutoConfigurationImportFilter {

    // Class names for both Spring Boot 3.x and 4.x; non-existent ones are filtered at init.
    private static final String[] ALL_EXCLUDE_CONFIGURATION = {
        // Spring Boot 3.x
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration$ResourceChainCustomizerConfiguration",
        "org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration$ResourceChainCustomizerConfiguration",
        // Spring Boot 4.x (add renamed classes here if they change)
    };

    public static final String[] EXCLUDE_CONFIGURATION;

    static {
        EXCLUDE_CONFIGURATION = Arrays.stream(ALL_EXCLUDE_CONFIGURATION)
            .filter(className -> {
                try {
                    Class.forName(className, false, ExcludeConfigurationFilter.class.getClassLoader());
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            })
            .toArray(String[]::new);
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] match = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            match[i] = !ArrayUtils.contains(EXCLUDE_CONFIGURATION, autoConfigurationClasses[i]);
        }
        return match;
    }

}

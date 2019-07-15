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
package demo.pf4j.shared.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Optional;

/**
 * Get {@link ApplicationContext> in static way. Since {@link ApplicationContext}
 * is bound to static class in this way, Plugin shouldn't use it directly, but
 * need to inherit it as a new privite class and register it in its own configuration.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext ctx = null;

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static <T> T getBean(Class<T> clazz) {
        return ctx.getBean(clazz);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        return (T) ctx.getBean(beanName);
    }

    public static String getMessage(String msgKey, Object...params) {
        try {
            return Optional.ofNullable(getBean(MessageSource.class)).map(bean -> bean.getMessage(
                    msgKey, params, LocaleContextHolder.getLocale())).orElse(msgKey);
        } catch (NoSuchMessageException ignored) {
            return msgKey;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        // Assign the ApplicationContext into a static method
        ApplicationContextProvider.ctx = ctx;
    }
}

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
package demo.sbp.library;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.laxture.sbp.SpringBootPlugin;
import org.laxture.sbp.spring.boot.SharedJtaSpringBootstrap;
import org.laxture.sbp.spring.boot.SpringBootstrap;
import org.pf4j.PluginWrapper;
import org.springframework.beans.BeansException;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class LibraryPlugin extends SpringBootPlugin {

    public LibraryPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    protected SpringBootstrap createSpringBootstrap() {
        return new SharedJtaSpringBootstrap(
                this, LibraryPluginStarter.class)
                .importBean("bookService");
    }
    @Override
    public void stop() {
        releaseAdditionalResources();
        super.stop();
    }

    @Override
    public void releaseAdditionalResources() {
        // close AtomikosDataSourceBean
//        DataSource dataSource = (DataSource)
//                getApplicationContext().getBean("dataSource");
//        try {
//            Method closeMethod;
//            try {
//                closeMethod = dataSource.getClass().getDeclaredMethod("close");
//            } catch (NoSuchMethodException e) { return; }
//            closeMethod.setAccessible(true);
//            closeMethod.invoke(dataSource);
//        } catch (BeansException | IllegalAccessException | InvocationTargetException ignored) {}

        AtomikosDataSourceBean dataSource = (AtomikosDataSourceBean)
            getApplicationContext().getBean("dataSource");
        dataSource.close();
    }
}
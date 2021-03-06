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
package demo.sbp.shelf;

import demo.sbp.api.service.AuthorService;
import demo.sbp.api.service.BookService;
import demo.sbp.api.service.PluginService;
import demo.sbp.shared.IdsConverter;
import demo.sbp.shelf.service.AuthorServiceMock;
import demo.sbp.shelf.service.BookServiceMock;
import demo.sbp.shelf.service.PluginServiceMock;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.NameTokenizers;
import org.modelmapper.jooq.RecordValueReader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SpringBootApplication
@EnableTransactionManagement
public class ShelfPluginStarter {

    public static void main(String[] args) {
        SpringApplication.run(ShelfPluginStarter.class, args);
    }

    @Bean
    @ConditionalOnMissingBean(name = "bookService")
    public BookService bookService() {
        return new BookServiceMock();
    }

    @Bean
    @ConditionalOnMissingBean(name = "authorService")
    public AuthorService authorService() {
        return new AuthorServiceMock();
    }

    @Bean
    @ConditionalOnMissingBean(name = "pluginService")
    public PluginService pluginService() {
        return new PluginServiceMock();
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setSourceNameTokenizer(NameTokenizers.CAMEL_CASE)
                .addValueReader(new RecordValueReader());
        mapper.addConverter(new IdsConverter());
        return mapper;
    }

}

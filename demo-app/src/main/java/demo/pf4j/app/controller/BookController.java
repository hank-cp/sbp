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
package demo.pf4j.app.controller;

import demo.pf4j.api.model.Book;
import org.jooq.DSLContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static demo.pf4j.app.tables.Tables.BOOK;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/book")
public class BookController {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ModelMapper mapper;

    @RequestMapping(value = "/")
    public @ResponseBody String index() {
        return "Hello World!";
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public @ResponseBody List<Book> listData() {
        return dslContext
                .selectFrom(BOOK)
                .fetch().stream()
                .map(e -> mapper.map(e, Book.class))
                .collect(Collectors.toList());
    }

}

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
package demo.pf4j.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.BookService;
import org.jooq.DSLContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static demo.pf4j.app.tables.Tables.BOOK;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Service("bookService")
public class BookServiceImpl implements BookService {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private ObjectMapper jsonMapper;

    @Override
    public Book getBook(long bookId) {
        return dslContext
                .selectFrom(BOOK)
                .where(BOOK.ID.eq(bookId))
                .fetchOne()
                .map(e -> mapper.map(e, Book.class));
    }

    @Override
    @Transactional
    public Book persistBook(String name) {
        return dslContext
                .insertInto(BOOK)
                .set(BOOK.NAME, name)
                .set(BOOK.EXTRA, jsonMapper.createObjectNode().put(
                        "uuid", UUID.randomUUID().toString()))
                .returning()
                .fetchOne().map(e -> mapper.map(e, Book.class));
    }
}

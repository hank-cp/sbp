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
package demo.sbp.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.sbp.api.model.Book;
import demo.sbp.api.service.BookService;
import demo.sbp.app.tables.Tables;
import org.jooq.DSLContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
                .selectFrom(Tables.BOOK)
                .where(Tables.BOOK.ID.eq(bookId))
                .fetchOne()
                .map(e -> mapper.map(e, Book.class));
    }

    @Override
    @Transactional
    public Book persistBook(String name) {
        return dslContext
                .insertInto(Tables.BOOK)
                .set(Tables.BOOK.NAME, name)
                .set(Tables.BOOK.EXTRA, jsonMapper.createObjectNode().put(
                        "uuid", UUID.randomUUID().toString()))
                .returning()
                .fetchOne().map(e -> mapper.map(e, Book.class));
    }

    @Override
    @Transactional
    public void deleteBook(long bookId) {
        dslContext.deleteFrom(Tables.BOOK)
                .where(Tables.BOOK.ID.eq(bookId))
                .execute();
    }
}

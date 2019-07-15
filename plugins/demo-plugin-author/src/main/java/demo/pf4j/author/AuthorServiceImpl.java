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
package demo.pf4j.author;

import demo.pf4j.api.extension.PluginRegister;
import demo.pf4j.api.model.Author;
import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.AuthorService;
import demo.pf4j.api.service.BookService;
import demo.pf4j.author.tables.Tables;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.modelmapper.ModelMapper;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Extension
@Service("authorService")
public class AuthorServiceImpl implements PluginRegister, AuthorService {

    @Override
    public String name() {
        return "author";
    }

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private BookService bookService;

    @Autowired
    private ModelMapper mapper;

    @Override
    public Author getAuthor(long id) {
        return dslContext.select(ArrayUtils.addAll(
                        Tables.AUTHOR.fields(),
                        DSL.listAgg(Tables.AUTHORBOOKS.BOOKID, ",")
                                .withinGroupOrderBy(Tables.AUTHORBOOKS.BOOKID)
                                .as("bookIds")))
                .from(Tables.AUTHOR)
                .leftJoin(Tables.AUTHORBOOKS)
                    .on(Tables.AUTHOR.ID.eq(Tables.AUTHORBOOKS.AUTHORID))
                .where(Tables.AUTHOR.ID.eq(id))
                .groupBy(Tables.AUTHOR.ID)
                .fetchOne(e -> {
                    Author author = mapper.map(e, Author.class);
                    author.books = author.bookIds.stream()
                            .map(bookId -> bookService.getBook(bookId))
                            .collect(Collectors.toList());
                    return author;
                });
    }

    @Override
    @Transactional
    public Author persistAuthor(String name, List<String> books) {
        Author author = dslContext
                .insertInto(Tables.AUTHOR)
                .set(Tables.AUTHOR.NAME, name)
                .returning()
                .fetchOne().map(e -> mapper.map(e, Author.class));

        if (CollectionUtils.isEmpty(books)) return author;

        books.forEach(bookName -> {
            Book book = bookService.persistBook(bookName);
            dslContext.insertInto(Tables.AUTHORBOOKS)
                    .set(Tables.AUTHORBOOKS.AUTHORID, author.id)
                    .set(Tables.AUTHORBOOKS.BOOKID, book.id)
                    .execute();
        });

        return author;
    }
}

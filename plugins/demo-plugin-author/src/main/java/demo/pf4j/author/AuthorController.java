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

import demo.pf4j.api.model.Author;
import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.AuthorService;
import demo.pf4j.api.service.BookService;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static demo.pf4j.author.tables.Tables.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/author")
public class AuthorController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ModelMapper mapper;

    @RequestMapping(value = "/list")
    public @ResponseBody List<Author> list() {
        return dslContext.select(ArrayUtils.addAll(
                        AUTHOR.fields(),
                        DSL.listAgg(AUTHORBOOKS.BOOKID, ",")
                                .withinGroupOrderBy(AUTHORBOOKS.BOOKID)
                                .as("bookIds")))
                .from(AUTHOR)
                .leftJoin(AUTHORBOOKS)
                .on(AUTHOR.ID.eq(AUTHORBOOKS.AUTHORID))
                .groupBy(AUTHOR.ID)
                .fetch(e -> {
                    Author author = mapper.map(e, Author.class);
                    author.books = author.bookIds.stream()
                            .map(bookId -> bookService.getBook(bookId))
                            .collect(Collectors.toList());
                    return author;
                });
    }

    @RequestMapping(value = "/new")
    @Transactional
    public @ResponseBody Author saveNew() {
        ArrayList<String> books = new ArrayList<>();
        books.add(UUID.randomUUID().toString());
        books.add(UUID.randomUUID().toString());
        return authorService.persistAuthor(UUID.randomUUID().toString(), books);
    }

    @RequestMapping(value = "/delete/{authorId}")
    @Transactional
    public @ResponseBody int delete(@PathVariable long authorId) {
        return dslContext.deleteFrom(AUTHOR)
                .where(AUTHOR.ID.eq(authorId))
                .execute();
    }

    @RequestMapping(value = "/failed")
    @Transactional
    public @ResponseBody List<Author> saveFailed() {
        Book book = bookService.getBook(1);
        ArrayList<String> books = new ArrayList<>();
        books.add(book.name);
        books.add(UUID.randomUUID().toString());
        authorService.persistAuthor(UUID.randomUUID().toString(), books);
        return dslContext.selectFrom(AUTHOR)
                .fetch().stream()
                .map(e -> mapper.map(e, Author.class))
                .collect(Collectors.toList());
    }
}
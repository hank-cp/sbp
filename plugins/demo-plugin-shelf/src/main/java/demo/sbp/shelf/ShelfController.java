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
import demo.sbp.shelf.model.Shelf;
import demo.sbp.shelf.tables.Tables;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/shelf")
public class ShelfController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ModelMapper mapper;

    @RequestMapping(value = "/list")
    public @ResponseBody List<Shelf> list() {
        return dslContext.select(ArrayUtils.addAll(
                    Tables.SHELF.fields(),
                    DSL.listAgg(Tables.SHELFBOOKS.BOOKID, ",")
                            .withinGroupOrderBy(Tables.SHELFBOOKS.BOOKID)
                            .as("bookIds"),
                    DSL.max(Tables.SHELFAUTHOR.AUTHORID).as("authorId")))
                .from(Tables.SHELF)
                .leftJoin(Tables.SHELFBOOKS).on(Tables.SHELF.ID.eq(Tables.SHELFBOOKS.SHELFID))
                .leftJoin(Tables.SHELFAUTHOR).on(Tables.SHELF.ID.eq(Tables.SHELFAUTHOR.SHELFID))
                .groupBy(Tables.SHELF.ID)
                .fetch(e -> {
                    Shelf shelf = mapper.map(e, Shelf.class);
                    shelf.books = shelf.bookIds.stream()
                            .map(bookId -> bookService.getBook(bookId))
                            .collect(Collectors.toList());
                    shelf.author = authorService.getAuthor(shelf.author.id);
                    return shelf;
                });
    }

    @RequestMapping(value = "/test-plugin-service")
    public @ResponseBody String testPluginService() {
        return pluginService.whoAmI();
    }
}
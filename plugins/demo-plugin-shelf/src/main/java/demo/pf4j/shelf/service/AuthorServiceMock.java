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
package demo.pf4j.shelf.service;

import demo.pf4j.api.model.Author;
import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.AuthorService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class AuthorServiceMock implements AuthorService {

    @Override
    public Author getAuthor(long id) {
        Author author = new Author();
        author.id = id;
        return author;
    }

    @Override
    public Author persistAuthor(String name, List<String> books) {
        Author author = new Author();
        author.name = name;
        author.books = books.stream().map(bookName -> {
            Book book = new Book();
            book.name = bookName;
            return book;
        }).collect(Collectors.toList());
        return author;
    }

}

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
package demo.pf4j.author.service;

import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.BookService;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
public class BookServiceMock implements BookService {

    @Override
    public Book getBook(long id) {
        Book author = new Book();
        author.id = id;
        return author;
    }

    @Override
    public Book persistBook(String name) {
        Book author = new Book();
        author.name = name;
        return author;
    }

    @Override
    public void deleteBook(long id) {

    }
}

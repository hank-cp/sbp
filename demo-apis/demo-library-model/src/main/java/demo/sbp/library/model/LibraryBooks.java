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
package demo.sbp.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.sbp.api.model.Book;
import demo.sbp.api.service.BookService;
import org.laxture.spring.util.ApplicationContextProvider;

import javax.persistence.*;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Entity
@Table(name = "LibraryBooks", schema = "plugin_library")
public class LibraryBooks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @ManyToOne(optional = false)
    @JsonIgnore
    public Library library;

    @Column(nullable = false)
    public long bookId;

    @Transient
    public Book book;

    @PostLoad
    private void postLoad() {
        BookService bookService = ApplicationContextProvider
                .getBean(LibraryBooks.class, BookService.class);
        book = bookService.getBook(bookId);
    }
}

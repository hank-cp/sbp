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
package demo.pf4j.library;

import demo.pf4j.api.model.Book;
import demo.pf4j.api.service.BookService;
import demo.pf4j.library.model.Library;
import demo.pf4j.library.model.LibraryBooks;
import demo.pf4j.library.repository.LibraryBooksRepository;
import demo.pf4j.library.repository.LibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@RestController
@RequestMapping(value = "/library")
public class LibraryController {

    @Autowired
    private BookService bookService;

    @Autowired
    private LibraryRepository libraryRepository;

    @Autowired
    private LibraryBooksRepository libraryBooksRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/list")
    @Transactional(readOnly = true)
    public @ResponseBody List<Library> list() {
        return libraryRepository.findAll();
    }

    @RequestMapping(value = "/books/{libraryId}")
    @Transactional(readOnly = true)
    public @ResponseBody List<Book> books(@PathVariable long libraryId) {
        return libraryBooksRepository.findByLibraryId(libraryId)
                .stream().map(libraryBooks -> libraryBooks.book)
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/new")
    @Transactional
    public @ResponseBody Library save() {
        Library library = new Library();
        library.code = UUID.randomUUID().toString();
        libraryRepository.save(library);

        LibraryBooks libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = 1;
        libraryBooksRepository.save(libraryBooks);

        libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = 2;
        libraryBooksRepository.save(libraryBooks);

        libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = 3;
        libraryBooksRepository.save(libraryBooks);

        // insert a new book for test
        Book book = bookService.persistBook(UUID.randomUUID().toString());
        libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = book.id;
        libraryBooksRepository.save(libraryBooks);

        return library;
    }

    @RequestMapping(value = "/failed")
    @Transactional
    public @ResponseBody Library failed() {
        Library library = new Library();
        library.code = UUID.randomUUID().toString();
        libraryRepository.save(library);

        LibraryBooks libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = 1;
        libraryBooksRepository.save(libraryBooks);

        // insert a new book for test and here should throw an exception
        Book book = bookService.getBook(1);
        book = bookService.persistBook(book.name);
        libraryBooks = new LibraryBooks();
        libraryBooks.library = library;
        libraryBooks.bookId = book.id;
        libraryBooksRepository.save(libraryBooks);

        return library;
    }

}
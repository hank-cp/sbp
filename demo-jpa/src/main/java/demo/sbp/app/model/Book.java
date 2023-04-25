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
package demo.sbp.app.model;

import jakarta.persistence.*;

import java.io.Serial;

/**
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@Entity
@Table(name = "Book", schema = "demo_jpa")
public class Book extends demo.sbp.api.model.Book {

    @Serial
    private static final long serialVersionUID = -6414703984967843742L;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return super.getId();
    }

    @Override
    @Column(unique = true, nullable = false)
    public String getName() {
        return super.getName();
    }
}

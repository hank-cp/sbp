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
package demo.pf4j.shared;

import org.jooq.Record;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Jooq OneToMany ids mapping.
 *
 * @author <a href="https://github.com/hank-cp">Hank CP</a>
 */
@SuppressWarnings("ConstantConditions")
public class IdsConverter implements ConditionalConverter<Record, List<Long>> {

    @Override
    public MatchResult match(Class<?> sourceType, Class<?> destinationType) {
        return Record.class.isAssignableFrom(sourceType)
                && Collection.class.isAssignableFrom(destinationType)
                ? MatchResult.FULL
                : MatchResult.NONE;
    }

    @Override
    public List<Long> convert(MappingContext<Record, List<Long>> context) {
        Object source = context.getSource();
        String idsInString = (String) source;
        if (idsInString == null || idsInString.length() <= 0)
            return new ArrayList<>();

        String[] ids = idsInString.split(",");
        return Arrays.stream(ids)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}

/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.generator;

import java.util.regex.Pattern;

public class SpecParser {

    @SuppressWarnings("unused")
    private AnnotationProcessingLogger logger;

    public SpecParser(AnnotationProcessingLogger logger) {
        this.logger = logger;
    }

    public String removeComments(String source) {
        source = source.replaceAll("(?sm)" + Pattern.quote("/*") + "" + Pattern.quote("*/"), "");
        source = source.replaceAll("(?sm)//.*?\n", "\n");
        return source;
    }

    public String[] parseWords(String source) {
        return removeComments(source).split("[^\\w_$]+");
    }
}

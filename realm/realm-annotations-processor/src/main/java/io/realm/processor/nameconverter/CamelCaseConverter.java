/*
 * Copyright 2018 Realm Inc.
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
package io.realm.processor.nameconverter;

/**
 * Converter that converts input to "camelCase".
 */
public class CamelCaseConverter implements NameConverter {

    private final WordTokenizer tokenizer = new WordTokenizer();

    @Override
    public String convert(String name) {
        String[] words = tokenizer.split(name);
        StringBuilder output = new StringBuilder();
        boolean firstWordEmitted = false;
        for (int i = 0; i < words.length; i++) {
            String word = words[i].toLowerCase();
            if (firstWordEmitted) {
                int codepoint = word.codePointAt(0);
                output.appendCodePoint(Character.toUpperCase(codepoint));
                output.append(word.substring(Character.charCount(codepoint)));
            } else {
                output.append(word);
                firstWordEmitted = true;
            }
        }

        return output.toString();
    }
}

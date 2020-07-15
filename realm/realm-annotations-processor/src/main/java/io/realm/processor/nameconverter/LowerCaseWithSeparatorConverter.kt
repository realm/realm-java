/*
 * Copyright 2019 Realm Inc.
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
package io.realm.processor.nameconverter

/**
 * Converter that converts input to lower case with a defined separator character.
 */
class LowerCaseWithSeparatorConverter(private val separator: Char) : NameConverter {

    private val tokenizer = WordTokenizer()

    override fun convert(name: String): String {
        val words = tokenizer.split(name)
        val output = StringBuilder()
        for (i in words.indices) {
            val word = words[i].toLowerCase()
            output.append(word)
            if (i < words.size - 1) {
                output.append(separator)
            }
        }

        return output.toString()
    }
}

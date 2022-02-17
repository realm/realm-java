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

import java.util.ArrayList

/**
 * Segments a Java variable name into component words.
 *
 * Java variable names must follow the rules described in:
 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8
 *
 * In this implementation we treat word separators as any of the following:
 *
 *  1. Anytime a `_` or `$` is encountered.
 *     Example is "_FooBar" or "_Foo$Bar" which both becomes "Foo" and "Bar".
 *
 *  2. Anytime you switch from a lower case character to an upper case character as identified by a
 *     `Character.isUpperCase(codepoint)` and `Character.isLowerCase(codepoint)`.
 *     Example is "FooBar" which becomes "Foo" and "Bar".
 *
 *  3. Anytime you switch from more than one uppercase character to a lower case one. As identified
 *     by `Character.isUpperCase(codepoint)` and `Character.isLowerCase(codepoint)`.
 *     Example is "FOOBar" which becomes "FOO" and "Bar.
 *
 *  4. Some characters like emojiis are neither uppercase or lowercase characters, so they will
 *     not trigger any of the above rules.
 *     Examples are "my😁" and "MY😁" which are both treated as one word.
 *
 *  5. Hungarian notation, i.e. strings starting with lowercase "m" followed by uppercase letter
 *     is stripped and not considered part of any word.
 */
class WordTokenizer {

    /**
     * Segments a string into words as described above
     */
    internal fun split(str: String?): Array<String> {
        if (str == null || str.isEmpty()) {
            return arrayOf()
        }

        var previousCodepoint: Int?
        var currentCodepoint: Int? = null
        val length = str.length
        var offset = 0
        val currentWord = StringBuilder()
        val words = ArrayList<String>()
        var wordAllUpperCase: Boolean? = null
        var lastCodePointCharLength = 0
        while (offset < length) {
            previousCodepoint = currentCodepoint
            currentCodepoint = str.codePointAt(offset)
            val currentCharCount = Character.charCount(currentCodepoint)
            val previousCodePointUpperCase = previousCodepoint != null && Character.isUpperCase(previousCodepoint)
            val previousCodePointLowerCase = previousCodepoint != null && Character.isLowerCase(previousCodepoint)
            val currentCodePointUpperCase = Character.isUpperCase(currentCodepoint)
            val currentCodePointLowerCase = Character.isLowerCase(currentCodepoint)

            // Separator char encountered not part of any word, but indicate a boundary
            if (currentCodepoint == '_'.toInt() || currentCodepoint == '$'.toInt()) {
                if (currentWord.isNotEmpty()) {
                    words.add(currentWord.toString())
                    currentWord.setLength(0)
                }

                wordAllUpperCase = null
                offset += currentCharCount
                lastCodePointCharLength = 0
                continue
            }

            // Change between lower case and upper case indicate a word boundary
            if (previousCodePointLowerCase && currentCodePointUpperCase) {
                if (currentWord.isNotEmpty()) {
                    words.add(currentWord.toString())
                    currentWord.setLength(0)
                    currentWord.appendCodePoint(currentCodepoint)
                }

                wordAllUpperCase = true
                offset += currentCharCount
                lastCodePointCharLength = currentCharCount
                continue
            }

            // Change between upper case and lower case indicated a word boundary on the previous
            // char if multiple upper case characters where encountered.
            if (currentWord.length > 1
                    && wordAllUpperCase != null && wordAllUpperCase
                    && previousCodePointUpperCase && currentCodePointLowerCase) {
                words.add(currentWord.substring(0, currentWord.length - lastCodePointCharLength))
                currentWord.substring(0, currentWord.length - lastCodePointCharLength)
                currentWord.delete(0, currentWord.length - lastCodePointCharLength)
                currentWord.appendCodePoint(currentCodepoint)

                wordAllUpperCase = false
                offset += currentCharCount
                lastCodePointCharLength = currentCharCount
                continue
            }

            // Add codepoint to current word
            currentWord.appendCodePoint(currentCodepoint)
            wordAllUpperCase = currentCodePointUpperCase && (wordAllUpperCase == null || wordAllUpperCase)
            offset += currentCharCount
            lastCodePointCharLength = currentCharCount
        }

        // Add final word when exiting loop
        if (currentWord.length > 0) {
            words.add(currentWord.toString())
        }

        // Remove hungarian notation if found
        if (words[0] == "m") {
            words.removeAt(0)
        }

        return words.toTypedArray()
    }
}

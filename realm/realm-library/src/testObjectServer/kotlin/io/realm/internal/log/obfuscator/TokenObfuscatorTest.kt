/*
 * Copyright 2020 Realm Inc.
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
package io.realm.internal.log.obfuscator

import io.realm.ObfuscatorHelper
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TokenObfuscatorTest {

    @Test
    fun obfuscate() {
        TokenObfuscator.obfuscator()
                .obfuscate(ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_GENERIC)
                .let { assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_GENERIC, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_APPLE)
                .let { assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_APPLE, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_FACEBOOK)
                .let { assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_FACEBOOK, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_GOOGLE)
                .let { assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_GOOGLE, it) }
    }

    @Test
    fun obfuscate_doesNothing() {
        TokenObfuscator.obfuscator()
                .obfuscate(ObfuscatorHelper.IRRELEVANT_INPUT)
                .let { assertEquals(ObfuscatorHelper.IRRELEVANT_INPUT, it) }
    }

    @Test
    fun obfuscate_fails() {
        assertFailsWith<NullPointerException> {
            TokenObfuscator.obfuscator().obfuscate(null)
        }
    }
}

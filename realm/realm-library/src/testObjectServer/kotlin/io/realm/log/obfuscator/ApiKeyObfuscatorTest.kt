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
package io.realm.log.obfuscator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val IRRELEVANT_INPUT = """{"blahblahblah":"blehblehbleh"}"""
private const val ORIGINAL_INPUT = """{"blahblahblah":"blehblehbleh","key":"my_key","something":"random"}"""
private const val OBFUSCATED_OUTPUT = """{"blahblahblah":"blehblehbleh","key":"***","something":"random"}"""

class ApiKeyObfuscatorTest {

    @Test
    fun obfuscate() {
        ApiKeyObfuscator.obfuscator()
                .obfuscate(ORIGINAL_INPUT)
                .let { assertEquals(OBFUSCATED_OUTPUT, it) }
    }

    @Test
    fun obfuscate_doesNothing() {
        ApiKeyObfuscator.obfuscator()
                .obfuscate(IRRELEVANT_INPUT)
                .let { assertEquals(IRRELEVANT_INPUT, it) }
    }

    @Test
    fun obfuscate_fails() {
        assertFailsWith<NullPointerException> {
            ApiKeyObfuscator.obfuscator().obfuscate(null)
        }
    }
}

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
package io.realm.mongodb.log.obfuscator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

val TOKEN_ORIGINAL_INPUT_GENERIC = """
{
  "blahblahblah":"blehblehbleh",
  "token":"my_token",
  "something":"random"
}
""".trimIndent()
val TOKEN_ORIGINAL_INPUT_APPLE = """
{
  "blahblahblah":"blehblehbleh",
  "id_token":"my_provider",
  "something":"random"
}
""".trimIndent()
val TOKEN_ORIGINAL_INPUT_FACEBOOK = """
{
  "blahblahblah":"blehblehbleh",
  "access_token":"my_access_token",
  "something":"random"
}
""".trimIndent()
val TOKEN_ORIGINAL_INPUT_GOOGLE = """
{
  "blahblahblah":"blehblehbleh",
  "authCode":"my_authCode",
  "something":"random"
}
""".trimIndent()
val TOKEN_OBFUSCATED_OUTPUT_GENERIC = """
{
  "blahblahblah":"blehblehbleh",
  "token":"***",
  "something":"random"
}
""".trimIndent()
val TOKEN_OBFUSCATED_OUTPUT_APPLE = """
{
  "blahblahblah":"blehblehbleh",
  "id_token":"***",
  "something":"random"
}
""".trimIndent()
val TOKEN_OBFUSCATED_OUTPUT_FACEBOOK = """
{
  "blahblahblah":"blehblehbleh",
  "access_token":"***",
  "something":"random"
}
""".trimIndent()
val TOKEN_OBFUSCATED_OUTPUT_GOOGLE = """
{
  "blahblahblah":"blehblehbleh",
  "authCode":"***",
  "something":"random"
}
""".trimIndent()

class TokenObfuscatorTest {

    @Test
    fun obfuscate() {
        TokenObfuscator.obfuscator()
                .obfuscate(TOKEN_ORIGINAL_INPUT_GENERIC)
                .let { assertEquals(TOKEN_OBFUSCATED_OUTPUT_GENERIC, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(TOKEN_ORIGINAL_INPUT_APPLE)
                .let { assertEquals(TOKEN_OBFUSCATED_OUTPUT_APPLE, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(TOKEN_ORIGINAL_INPUT_FACEBOOK)
                .let { assertEquals(TOKEN_OBFUSCATED_OUTPUT_FACEBOOK, it) }
        TokenObfuscator.obfuscator()
                .obfuscate(TOKEN_ORIGINAL_INPUT_GOOGLE)
                .let { assertEquals(TOKEN_OBFUSCATED_OUTPUT_GOOGLE, it) }
    }

    @Test
    fun obfuscate_doesNothing() {
        TokenObfuscator.obfuscator()
                .obfuscate(IRRELEVANT_INPUT)
                .let { assertEquals(IRRELEVANT_INPUT, it) }
    }

    @Test
    fun obfuscate_fails() {
        assertFailsWith<NullPointerException> {
            TokenObfuscator.obfuscator().obfuscate(null)
        }
    }
}

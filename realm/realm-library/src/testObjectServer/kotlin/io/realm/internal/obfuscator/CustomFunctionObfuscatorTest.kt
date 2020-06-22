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
package io.realm.internal.obfuscator

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomFunctionObfuscatorTest {

    @Test
    fun obfuscate() {
        CustomFunctionObfuscator.obfuscator()
                .obfuscate(CUSTOM_FUNCTION_ORIGINAL_INPUT)
                .let { assertEquals(CUSTOM_FUNCTION_OBFUSCATED_OUTPUT, it) }
    }

    @Test
    fun obfuscate_doesNothing() {
        CustomFunctionObfuscator.obfuscator()
                .obfuscate(IRRELEVANT_INPUT)
                .let { assertEquals(IRRELEVANT_INPUT, it) }
    }

    @Test
    fun obfuscate_fails() {
        assertFailsWith<NullPointerException> {
            CustomFunctionObfuscator.obfuscator().obfuscate(null)
        }
    }

    companion object {
        val CUSTOM_FUNCTION_ORIGINAL_INPUT = """
{
  "mail":"myfakemail@mongodb.com",
  "id":{
    "{${'$'}}numberInt": "666"
  },
  "options":{
    "device":{
      "appVersion":"1.0.",
      "appId":"realm-sdk-integration-tests-grbrc",
      "platform":"android",
      "platformVersion":"10",
      "sdkVersion":"10.0.0-BETA.5-SNAPSHOT"
    }
  }
}
""".trimStartMultiline()

        val CUSTOM_FUNCTION_OBFUSCATED_OUTPUT = """
{
  "functionArgs":"***",
  "options":{
    "device":{
      "appVersion":"1.0.",
      "appId":"realm-sdk-integration-tests-grbrc",
      "platform":"android",
      "platformVersion":"10",
      "sdkVersion":"10.0.0-BETA.5-SNAPSHOT"
    }
  }
}
""".trimStartMultiline()

        private fun String.trimStartMultiline(): String {
            return this.split("\n").joinToString(separator = "") { it.trimStart() }
        }
    }
}

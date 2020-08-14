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

package io.realm

object ObfuscatorHelper {
    const val IRRELEVANT_INPUT = """{"blahblahblah":"blehblehbleh"}"""

    val API_KEY_ORIGINAL_INPUT = """
{
  "blahblahblah":"blehblehbleh",
  "key":"my_key",
  "something":"random"
}
""".trimIndent()
    val API_KEY_OBFUSCATED_OUTPUT = """
{
  "blahblahblah":"blehblehbleh",
  "key":"***",
  "something":"random"
}
""".trimIndent()

    val EMAIL_PASSWORD_ORIGINAL_INPUT = """
{
  "blahblahblah":"blehblehbleh",
  "username":"my_username",
  "password":"123456",
  "something":"random"
}
""".trimIndent()
    val EMAIL_PASSWORD_OBFUSCATED_OUTPUT = """
{
  "blahblahblah":"blehblehbleh",
  "username":"***",
  "password":"***",
  "something":"random"
}
""".trimIndent()

    val CUSTOM_FUNCTION_ORIGINAL_INPUT = """
{
  "mail":"myfakemail@mongodb.com",
  "id":{
    "{${'$'}}numberInt": 666"
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
  "accessToken":"my_access_token",
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
  "accessToken":"***",
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

    private fun String.trimStartMultiline(): String {
        return this.split("\n").joinToString(separator = "") { it.trimStart() }
    }
}

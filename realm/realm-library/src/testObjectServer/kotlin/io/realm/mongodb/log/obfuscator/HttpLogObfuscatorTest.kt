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

import io.realm.ObfuscatorHelper
import io.realm.mongodb.AppConfiguration
import org.junit.Test
import kotlin.test.assertEquals

const val IRRELEVANT_INPUT = """{"blahblahblah":"blehblehbleh"}"""
const val FEATURE = "providers"

class HttpLogObfuscatorTest {

    private val apiKeyUrlSegments = listOf(FEATURE, "api-key")
    private val customFunctionUrlSegments = listOf(FEATURE, "custom-function")
    private val emailPasswordUrlSegments = listOf(FEATURE, "local-userpass")
    private val tokenUrlSegmentsApple = listOf(FEATURE, "oauth2-apple")
    private val tokenUrlSegmentsFacebook = listOf(FEATURE, "oauth2-facebook")
    private val tokenUrlSegmentsGoogle = listOf(FEATURE, "oauth2-google")

    private val loginObfuscators = AppConfiguration.loginObfuscators

    @Test
    fun obfuscate_nothing() {
        with(HttpLogObfuscator(FEATURE, mapOf())) {
            assertEquals(IRRELEVANT_INPUT, obfuscate(listOf(), IRRELEVANT_INPUT))
        }
    }

    @Test
    fun obfuscate_apiKey() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.API_KEY_OBFUSCATED_OUTPUT, obfuscate(apiKeyUrlSegments, ObfuscatorHelper.API_KEY_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_customFunction() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.CUSTOM_FUNCTION_OBFUSCATED_OUTPUT, obfuscate(customFunctionUrlSegments, ObfuscatorHelper.CUSTOM_FUNCTION_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_emailPassword() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.EMAIL_PASSWORD_OBFUSCATED_OUTPUT, obfuscate(emailPasswordUrlSegments, ObfuscatorHelper.EMAIL_PASSWORD_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_tokenApple() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_APPLE, obfuscate(tokenUrlSegmentsApple, ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_APPLE))
        }
    }

    @Test
    fun obfuscate_tokenFacebook() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_FACEBOOK, obfuscate(tokenUrlSegmentsFacebook, ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_FACEBOOK))
        }
    }

    @Test
    fun obfuscate_tokenGoogle() {
        with(HttpLogObfuscator(FEATURE, loginObfuscators)) {
            assertEquals(ObfuscatorHelper.TOKEN_OBFUSCATED_OUTPUT_GOOGLE, obfuscate(tokenUrlSegmentsGoogle, ObfuscatorHelper.TOKEN_ORIGINAL_INPUT_GOOGLE))
        }
    }
}

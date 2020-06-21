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

import io.realm.mongodb.PatternObfuscatorFactory
import org.junit.Test
import kotlin.test.assertEquals

const val IRRELEVANT_INPUT = """{"blahblahblah":"blehblehbleh"}"""

class LoginInfoObfuscatorTest {

    private val apiKeyUrlSegments = listOf("providers", "api-key")
    private val customFunctionUrlSegments = listOf("providers", "custom-function")
    private val emailPasswordUrlSegments = listOf("providers", "local-userpass")
    private val tokenUrlSegmentsApple = listOf("providers", "oauth2-apple")
    private val tokenUrlSegmentsFacebook = listOf("providers", "oauth2-facebook")
    private val tokenUrlSegmentsGoogle = listOf("providers", "oauth2-google")

    @Test
    fun obfuscate_nothing() {
        with(LoginInfoObfuscator(mapOf())) {
            assertEquals(IRRELEVANT_INPUT, obfuscate(listOf(), IRRELEVANT_INPUT))
        }
    }

    @Test
    fun obfuscate_apiKey() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(API_KEY_OBFUSCATED_OUTPUT, obfuscate(apiKeyUrlSegments, API_KEY_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_customFunction() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(CUSTOM_FUNCTION_OBFUSCATED_OUTPUT, obfuscate(customFunctionUrlSegments, CUSTOM_FUNCTION_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_emailPassword() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(EMAIL_PASSWORD_OBFUSCATED_OUTPUT, obfuscate(emailPasswordUrlSegments, EMAIL_PASSWORD_ORIGINAL_INPUT))
        }
    }

    @Test
    fun obfuscate_tokenApple() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(TOKEN_OBFUSCATED_OUTPUT, obfuscate(tokenUrlSegmentsApple, TOKEN_ORIGINAL_INPUT_APPLE))
        }
    }

    @Test
    fun obfuscate_tokenFacebook() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(TOKEN_OBFUSCATED_OUTPUT, obfuscate(tokenUrlSegmentsFacebook, TOKEN_ORIGINAL_INPUT_FACEBOOK))
        }
    }

    @Test
    fun obfuscate_tokenGoogle() {
        with(LoginInfoObfuscator(PatternObfuscatorFactory.getObfuscators())) {
            assertEquals(TOKEN_OBFUSCATED_OUTPUT, obfuscate(tokenUrlSegmentsGoogle, TOKEN_ORIGINAL_INPUT_GOOGLE))
        }
    }
}

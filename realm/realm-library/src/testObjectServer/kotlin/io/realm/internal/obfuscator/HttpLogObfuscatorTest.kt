///*
// * Copyright 2020 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
package io.realm.internal.obfuscator
//
//import io.realm.mongodb.RegexObfuscatorPatternFactory
//import org.junit.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertFailsWith
//
const val IRRELEVANT_INPUT = """{"blahblahblah":"blehblehbleh"}"""
//const val FEATURE = "providers"
//
//class HttpLogObfuscatorTest {
//
//    private val apiKeyUrlSegments = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "api-key")
//    private val customFunctionUrlSegments = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "custom-function")
//    private val emailPasswordUrlSegments = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "local-userpass")
//    private val tokenUrlSegmentsApple = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "oauth2-apple")
//    private val tokenUrlSegmentsFacebook = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "oauth2-facebook")
//    private val tokenUrlSegmentsGoogle = listOf(RegexObfuscatorPatternFactory.LOGIN_FEATURE, "oauth2-google")
//
//    private val loginObfuscators = RegexObfuscatorPatternFactory.getObfuscators(RegexObfuscatorPatternFactory.LOGIN_FEATURE)
//
//    @Test
//    fun obfuscate_throwsNoFeature() {
//        assertFailsWith<IllegalArgumentException> {
//            HttpLogObfuscator.obfuscator(null, mapOf())
//        }
//    }
//
//    @Test
//    fun obfuscate_throwsNoPatterns() {
//        assertFailsWith<IllegalArgumentException> {
//            HttpLogObfuscator.obfuscator("blahblah", null)
//        }
//    }
//
//    @Test
//    fun obfuscate_nothing() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, mapOf())) {
//            assertEquals(IRRELEVANT_INPUT, obfuscate(listOf(), IRRELEVANT_INPUT))
//        }
//    }
//
//    @Test
//    fun obfuscate_apiKey() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(API_KEY_OBFUSCATED_OUTPUT, obfuscate(apiKeyUrlSegments, API_KEY_ORIGINAL_INPUT))
//        }
//    }
//
//    @Test
//    fun obfuscate_customFunction() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(CUSTOM_FUNCTION_OBFUSCATED_OUTPUT, obfuscate(customFunctionUrlSegments, CUSTOM_FUNCTION_ORIGINAL_INPUT))
//        }
//    }
//
//    @Test
//    fun obfuscate_emailPassword() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(EMAIL_PASSWORD_OBFUSCATED_OUTPUT, obfuscate(emailPasswordUrlSegments, EMAIL_PASSWORD_ORIGINAL_INPUT))
//        }
//    }
//
//    @Test
//    fun obfuscate_tokenApple() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(TOKEN_OBFUSCATED_OUTPUT_APPLE, obfuscate(tokenUrlSegmentsApple, TOKEN_ORIGINAL_INPUT_APPLE))
//        }
//    }
//
//    @Test
//    fun obfuscate_tokenFacebook() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(TOKEN_OBFUSCATED_OUTPUT_FACEBOOK, obfuscate(tokenUrlSegmentsFacebook, TOKEN_ORIGINAL_INPUT_FACEBOOK))
//        }
//    }
//
//    @Test
//    fun obfuscate_tokenGoogle() {
//        with(HttpLogObfuscator.obfuscator(FEATURE, loginObfuscators)) {
//            assertEquals(TOKEN_OBFUSCATED_OUTPUT_GOOGLE, obfuscate(tokenUrlSegmentsGoogle, TOKEN_ORIGINAL_INPUT_GOOGLE))
//        }
//    }
//}

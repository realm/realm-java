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

package io.realm.internal.log.obfuscator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Obfuscator for oAuth2 token-related login requests.
 * <p>
 * It will replace the
 * <ul>
 * <li>{@code "authCode":"<TOKEN>"},</li>
 * <li>{@code "id_token":"<TOKEN>"},</li>
 * <li>{@code "token":"<TOKEN>"}, and</li>
 * <li>{@code "access_token":"<TOKEN>"}</li>
 * </ul>
 * patterns with
 * <ul>
 * <li>{@code "authCode":"***"},</li>
 * <li>{@code "id_token":"***"},</li>
 * <li>{@code "token":"***"}, and</li>
 * <li>{@code "access_token":"***"}</li>
 * </ul>
 * respectively.
 */
public class TokenObfuscator extends RegexPatternObfuscator {

    public static final String AUTHCODE_KEY = "authCode";
    public static final String ID_TOKEN_KEY = "id_token";
    public static final String TOKEN_KEY = "token";
    public static final String ACCESS_TOKEN_KEY = "accessToken";

    private TokenObfuscator(Map<Pattern, String> patternReplacementMap) {
        super(patternReplacementMap);
    }

    /**
     * Creates a {@link RegexPatternObfuscator} for tokens.
     *
     * @return an obfuscator that keeps token information from being displayed in the logcat.
     */
    public static TokenObfuscator obfuscator() {
        return new TokenObfuscator(getPatterns());
    }

    private static Map<Pattern, String> getPatterns() {
        Map<Pattern, String> map = new HashMap<>();
        map.put(Pattern.compile("((\"" + AUTHCODE_KEY + "\"):(\".+?\"))"), "\"" + AUTHCODE_KEY + "\":\"***\"");
        map.put(Pattern.compile("((\"" + ID_TOKEN_KEY + "\"):(\".+?\"))"), "\"" + ID_TOKEN_KEY + "\":\"***\"");
        map.put(Pattern.compile("((\"" + TOKEN_KEY + "\"):(\".+?\"))"), "\"" + TOKEN_KEY + "\":\"***\"");
        map.put(Pattern.compile("((\"" + ACCESS_TOKEN_KEY + "\"):(\".+?\"))"), "\"" + ACCESS_TOKEN_KEY + "\":\"***\"");
        return map;
    }
}

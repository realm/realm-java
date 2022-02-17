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
 * Obfuscator for email- and password-related login requests.
 * <p>
 * It will replace the
 * <ul>
 * <li>{@code "email":"<EMAIL>"},</li>
 * <li>{@code "username":"<USERNAME>"} and</li>
 * <li>{@code "password":"<PASSWORD>"}</li>
 * </ul>
 * patterns with
 * <ul>
 * <li>{@code "email":"***"},</li>
 * <li>{@code "username":"***"} and</li>
 * <li>{@code "password":"***"}</li>
 * </ul>
 * respectively.
 */
public class EmailPasswordObfuscator extends RegexPatternObfuscator {

    public static final String EMAIL_KEY = "email";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";

    private EmailPasswordObfuscator(Map<Pattern, String> patternReplacementMap) {
        super(patternReplacementMap);
    }

    /**
     * Creates a {@link RegexPatternObfuscator} for emails and passwords.
     *
     * @return an obfuscator that keeps emails and passwords from being displayed in the logcat.
     */
    public static EmailPasswordObfuscator obfuscator() {
        return new EmailPasswordObfuscator(getPatterns());
    }

    private static Map<Pattern, String> getPatterns() {
        Map<Pattern, String> map = new HashMap<>();
        map.put(Pattern.compile("((\"" + EMAIL_KEY + "\"):(\".+?\"))"), "\"" + EMAIL_KEY + "\":\"***\"");
        map.put(Pattern.compile("((\"" + USERNAME_KEY + "\"):(\".+?\"))"), "\"" + USERNAME_KEY + "\":\"***\"");
        map.put(Pattern.compile("((\"" + PASSWORD_KEY + "\"):(\".+?\"))"), "\"" + PASSWORD_KEY + "\":\"***\"");
        return map;
    }
}

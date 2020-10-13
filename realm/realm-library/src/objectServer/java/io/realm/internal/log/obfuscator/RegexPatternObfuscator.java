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

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.internal.Util;

/**
 * The obfuscator removes sensitive information from logcat entries.
 * <p>
 * Children classes have to provide a map of regex {@link Pattern}s and replacement strings to
 * correctly hide the information.
 * <p>
 * For example, the following pattern finds instances of {@code "token":"<TOKEN_GOES_HERE>"} in the
 * logcat: {@code Pattern.compile("((\"token\"):(\".+?\"))")}. And the replacement string
 * {@code "\"token\":\"***\""} replaces those instances with {@code "token":"***"}.
 */
public abstract class RegexPatternObfuscator {

    private Map<Pattern, String> patternReplacementMap;

    RegexPatternObfuscator(Map<Pattern, String> patternReplacementMap) {
        this.patternReplacementMap = patternReplacementMap;
    }

    /**
     * Obfuscates a string according to the patterns and replacements an obfuscator has.
     *
     * @param input the string to obfuscate
     * @return the obfuscate string
     */
    public String obfuscate(String input) {
        String obfuscatedString = input;
        Set<Map.Entry<Pattern, String>> entries = patternReplacementMap.entrySet();
        for (Map.Entry<Pattern, String> entry : entries) {
            String replacement = entry.getValue();
            Pattern pattern = entry.getKey();
            Util.checkNull(replacement, "replacement");
            Matcher matcher = pattern.matcher(obfuscatedString);
            obfuscatedString = matcher.replaceFirst(replacement);
        }
        return obfuscatedString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegexPatternObfuscator)) return false;
        RegexPatternObfuscator that = (RegexPatternObfuscator) o;
        return patternReplacementMap.equals(that.patternReplacementMap);
    }

    @Override
    public int hashCode() {
        return patternReplacementMap.hashCode() + 13;
    }
}

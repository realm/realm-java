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

package io.realm.log.obfuscator;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.internal.Util;

/**
 * The obfuscator hides sensitive information from being displayed in the logcat.
 * <p>
 * Children classes have to provide a map of regex {@link Pattern}s and replacement strings to
 * correctly hide the information.
 * <p>
 * For example, the following pattern finds instances of {@code "token":"<TOKEN_GOES_HERE>"} in the
 * logcat: {@code Pattern.compile("((\"token\"):(\".+?\"))")}. And the replacement string
 * {@code "\"token\":\"***\""} replaces those instances with {@code "token":"***"}, which
 * effectively keeps the token from being displayed in the logcat.
 */
public abstract class LogObfuscator {

    private Map<Pattern, String> patternReplacementMap;

    LogObfuscator(Map<Pattern, String> patternReplacementMap) {
        this.patternReplacementMap = patternReplacementMap;
    }

    String obfuscate(String input) {
        String obfuscatedString = input;
        for (Pattern pattern : patternReplacementMap.keySet()) {
            String replacement = patternReplacementMap.get(pattern);
            Util.checkNull(replacement, "replacement");
            Matcher matcher = pattern.matcher(obfuscatedString);
            obfuscatedString = matcher.replaceFirst(replacement);
        }
        return obfuscatedString;
    }

    /**
     * FIXME
     *
     * @param urlSegments
     * @param logObfuscators
     * @param obfuscatedOutput
     * @return
     */
    public static String obfuscate(List<String> urlSegments,
                                   Map<String, LogObfuscator> logObfuscators,
                                   String obfuscatedOutput) {
        if (urlSegments.contains("providers")) {
            String provider = urlSegments.get(urlSegments.indexOf("providers") + 1);
            LogObfuscator logObfuscator = logObfuscators.get(provider);
            if (logObfuscator != null) {
                return logObfuscator.obfuscate(obfuscatedOutput);
            }
        }
        return obfuscatedOutput;
    }
}

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

/**
 * The LoginInfoObfuscator keeps sensitive login information from being displayed in the logcat.
 */
public class LoginInfoObfuscator {

    private Map<String, PatternObfuscator> patternObfuscatorMap;

    /**
     * {@link Map} of identity providers and {@link PatternObfuscator}s used to determine which
     * obfuscator has to be used.
     *
     * @param patternObfuscatorMap the map of credential identity providers and obfuscators.
     */
    public LoginInfoObfuscator(Map<String, PatternObfuscator> patternObfuscatorMap) {
        this.patternObfuscatorMap = patternObfuscatorMap;
    }

    /**
     * Obfuscates a logcat entry or not depending on whether the request that is being sent is a
     * login request. If it is, its full path must contain a {@code providers} segment. If it is not
     * the logcat entry will not be obfuscated
     * <p>
     * Once extracted the identity provider its corresponding {@link PatternObfuscator} will carry
     * out the obfuscation.
     *
     * @param urlSegments the URL segments of the request to be sent.
     * @param input       the original logcat entry.
     * @return the obfuscated logcat entry.
     */
    public String obfuscate(List<String> urlSegments, String input) {
        if (urlSegments.contains("providers")) {
            String provider = urlSegments.get(urlSegments.indexOf("providers") + 1);
            PatternObfuscator patternObfuscator = patternObfuscatorMap.get(provider);
            if (patternObfuscator != null) {
                return patternObfuscator.obfuscate(input);
            }
        }
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoginInfoObfuscator)) return false;
        LoginInfoObfuscator that = (LoginInfoObfuscator) o;
        return patternObfuscatorMap.equals(that.patternObfuscatorMap);
    }

    @Override
    public int hashCode() {
        return patternObfuscatorMap.hashCode() + 13;
    }
}

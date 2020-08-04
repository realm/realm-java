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

package io.realm.mongodb.log.obfuscator;

import java.util.List;
import java.util.Map;

import io.realm.internal.Util;
import io.realm.internal.log.obfuscator.RegexPatternObfuscator;

/**
 * The HttpLogObfuscator keeps sensitive information from being displayed in Logcat.
 */
public class HttpLogObfuscator {

    private String feature;
    private Map<String, RegexPatternObfuscator> patternObfuscatorMap;

    /**
     * Constructor for creating an HTTP log obfuscator.
     *
     * @param feature              the feature to obfuscate, e.g. "providers" for login requests -
     *                             see {@link io.realm.internal.network.LoggingInterceptor}.
     * @param patternObfuscatorMap {@link Map} of keys subject to being obfuscated and
     *                             {@link RegexPatternObfuscator}s used to determine which
     *                             obfuscator has to be used for the given feature.
     */
    public HttpLogObfuscator(String feature, Map<String, RegexPatternObfuscator> patternObfuscatorMap) {
        Util.checkNull(feature, "feature");
        this.feature = feature;
        Util.checkNull(patternObfuscatorMap, "patternObfuscatorMap");
        this.patternObfuscatorMap = patternObfuscatorMap;
    }

    /**
     * Obfuscates a logcat entry or not depending on whether the request being sent matches the
     * specified feature. If it doesn't, the logcat entry will be returned unmodified.
     *
     * @param urlSegments the URL segments of the request to be sent.
     * @param input       the original logcat entry.
     * @return the logcat entry to be shown in the logcat.
     */
    public String obfuscate(List<String> urlSegments, String input) {
        int featureIndex = urlSegments.indexOf(feature);
        if (featureIndex != -1) {
            String value = urlSegments.get(featureIndex + 1);    // value is in the next segment
            RegexPatternObfuscator patternObfuscator = patternObfuscatorMap.get(value);
            if (patternObfuscator != null) {
                return patternObfuscator.obfuscate(input);
            }
        }
        return input;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpLogObfuscator)) return false;
        HttpLogObfuscator that = (HttpLogObfuscator) o;
        return patternObfuscatorMap.equals(that.patternObfuscatorMap);
    }

    @Override
    public int hashCode() {
        return patternObfuscatorMap.hashCode() + 13;
    }
}

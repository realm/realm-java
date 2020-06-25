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

import java.util.HashMap;
import java.util.Map;

import io.realm.mongodb.Credentials;

/**
 * The RegexPatternObfuscatorFactory provides the {@link RegexPatternObfuscator}s needed to
 * obfuscate HTTP requests being logged for a particular feature.
 */
public class RegexPatternObfuscatorFactory {

    public static final String LOGIN_FEATURE = "providers";

    private static Map<String, RegexPatternObfuscator> loginObfuscators =
            new HashMap<String, RegexPatternObfuscator>() {{
                put(Credentials.IdentityProvider.API_KEY.getId(), ApiKeyObfuscator.obfuscator());
                put(Credentials.IdentityProvider.SERVER_API_KEY.getId(), ApiKeyObfuscator.obfuscator());
                put(Credentials.IdentityProvider.APPLE.getId(), TokenObfuscator.obfuscator());
                put(Credentials.IdentityProvider.CUSTOM_FUNCTION.getId(), CustomFunctionObfuscator.obfuscator());
                put(Credentials.IdentityProvider.EMAIL_PASSWORD.getId(), EmailPasswordObfuscator.obfuscator());
                put(Credentials.IdentityProvider.FACEBOOK.getId(), TokenObfuscator.obfuscator());
                put(Credentials.IdentityProvider.GOOGLE.getId(), TokenObfuscator.obfuscator());
                put(Credentials.IdentityProvider.JWT.getId(), TokenObfuscator.obfuscator());
            }};

    /**
     * Provides a {@link Map} of strings representing sensitive information that should be
     * obfuscated and {@link RegexPatternObfuscator}s corresponding to a concrete feature to be used
     * in a {@link io.realm.mongodb.App}.
     * <p>
     * For example, if we want to hide all sensitive information regarding login credentials, we
     * have to pass a {@code providers} string as a feature. The factory will in turn provide a map
     * of logcat entries susceptible to being obfuscated for that very feature and the obfuscators
     * that will carry out the obfuscation itself.
     *
     * @param feature the feature to obfuscate.
     * @return the obfuscators that will be used in the app.
     */
    public static Map<String, RegexPatternObfuscator> getObfuscators(String feature) {
        if (feature.equals(LOGIN_FEATURE)) {
            return loginObfuscators;
        }
        throw new IllegalArgumentException("No feature found for " + feature);
    }
}

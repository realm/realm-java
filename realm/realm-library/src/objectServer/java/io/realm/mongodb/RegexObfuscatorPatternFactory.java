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

package io.realm.mongodb;

import java.util.HashMap;
import java.util.Map;

import io.realm.internal.obfuscator.ApiKeyObfuscator;
import io.realm.internal.obfuscator.CustomFunctionObfuscator;
import io.realm.internal.obfuscator.EmailPasswordObfuscator;
import io.realm.internal.obfuscator.RegexPatternObfuscator;
import io.realm.internal.obfuscator.TokenObfuscator;

public class RegexObfuscatorPatternFactory {

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
     * Provides the {@link Map} of providers and {@link RegexPatternObfuscator}s corresponding to a
     * concrete feature to be used in a {@link io.realm.mongodb.App}.
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

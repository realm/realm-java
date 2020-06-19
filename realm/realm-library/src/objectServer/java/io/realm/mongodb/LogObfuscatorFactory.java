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

import io.realm.log.obfuscator.ApiKeyObfuscator;
import io.realm.log.obfuscator.CustomFunctionObfuscator;
import io.realm.log.obfuscator.EmailPasswordObfuscator;
import io.realm.log.obfuscator.LogObfuscator;
import io.realm.log.obfuscator.TokenObfuscator;

public class LogObfuscatorFactory {

    private static Map<String, LogObfuscator> logObfuscators =
            new HashMap<String, LogObfuscator>() {{
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
     * Provides a {@link Map} of providers and {@link LogObfuscator}s to be used in a
     * {@link io.realm.mongodb.App}.
     *
     * @return the obfuscators that will be used in the app.
     */
    public static Map<String, LogObfuscator> getObfuscators() {
        return logObfuscators;
    }
}

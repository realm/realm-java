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
package io.realm;

import android.content.Context;

import javax.annotation.Nullable;

import io.realm.log.LogLevel;

public class RealmAppConfiguration {


    public static class Builder {

        public Builder(Context context, String appId) {

        }

        public Builder logLevel(LogLevel level) {
            return this;
        }

        public Builder encryptionKey(byte[] key) {
            return this;
        }

        public Builder defaultSessionErrorHandler(@Nullable SyncSession.ErrorHandler errorHandler) {
            return this;
        }

        public RealmAppConfiguration build() {
            return null;
        }
    }
}

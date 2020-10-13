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

package io.realm.internal.network;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.mongodb.AppException;

public class ResultHandler {

    // Handle returning the correct result or throw an exception. Must be separated from
    // OsJNIResultCallback due to how the Object Store callbacks work.
    public static <T> T handleResult(@Nullable AtomicReference<T> success, AtomicReference<AppException> error) {
        if (error.get() != null) {
            throw error.get();
        } else {
            if (success != null) {
                return success.get();
            } else {
                return null;
            }
        }
    }
}

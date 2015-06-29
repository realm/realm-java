/*
 * Copyright 2015 Realm Inc.
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

package io.realm.internal.async;

import io.realm.exceptions.RealmException;

/**
 * Triggered when the result of a query could not be used against the current state of the Realm
 * which might be more up-to-date than the provided results
 */
// Triggered from JNI level to indicate a failing Handover due to version mismatch
public class UnreachableVersionException extends RealmException {

    public UnreachableVersionException(String detailMessage) {
        super(detailMessage);
    }

    public UnreachableVersionException(String detailMessage, Throwable exception) {
        super(detailMessage, exception);
    }
}

/*
 * Copyright 2016 Realm Inc.
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

package io.realm.exceptions;

import io.realm.internal.Keep;

/**
 * Triggered from the JNI level when there was something wrong with the lock file.
 * This can happen if two different versions of Realm tries to access the same file concurrently.
 */
@Keep
public class IncompatibleLockFileException extends RealmIOException {

    public IncompatibleLockFileException(String detailMessage) {
        super(detailMessage);
    }

    public IncompatibleLockFileException(String detailMessage, Throwable exception) {
        super(detailMessage, exception);
    }
}

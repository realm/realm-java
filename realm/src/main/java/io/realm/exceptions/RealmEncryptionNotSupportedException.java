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

package io.realm.exceptions;

/**
 * On some devices (HTC One X for example), for some reason, the system doesn't pass the right
 * parameter (siginfo_t.si_addr) to the segfault signal handler which our encryption mechanism
 * relies on. Realm will try to detect if this problem exists on the device when an encrypted
 * Realm is being created/opened. A RealmEncryptionNotSupportedException will be thrown if this
 * problem exists which means that encryption cannot be used on this device.
 */
@SuppressWarnings("unused") // Thrown by JNI
public class RealmEncryptionNotSupportedException extends RuntimeException {
    public RealmEncryptionNotSupportedException(String message) {
        super(message);
    }
}

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

import org.bson.BsonValue;

import io.realm.internal.jni.OSJNIBsonProtocol;

public class RealmFunctions {

    // FIXME Prelimiry implementation to be able to test passing BsonValues through JNI
    BsonValue invoke(BsonValue arg) {
        String response = nativeCallFunction(OSJNIBsonProtocol.encode(arg));
        return OSJNIBsonProtocol.decode(response);
    }

    private static native String nativeCallFunction(String arg);

}

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
import org.bson.codecs.configuration.CodecRegistry;

import io.realm.internal.jni.JniBsonProtocol;

// FIXME This class is only a placeholder for JNI round trip as  until actual RealmFunctions
//  implementation supersedes it.
class RealmFunctions {

    // FIXME Prelimiry implementation to be able to test passing BsonValues through JNI
    BsonValue invoke(BsonValue arg) {
        String response = nativeCallFunction(JniBsonProtocol.encode(arg));
        return JniBsonProtocol.decode(response);
    }

    // FIXME Prelimiry implementation to be able to test passing BsonValues through JNI
    <T> T invoke(Object arg, Class<T> resultClass, CodecRegistry registry) {
        String a = JniBsonProtocol.encode(arg, registry);
        String s = nativeCallFunction(a);
        return JniBsonProtocol.decode(s, resultClass, registry);
    }

    private static native String nativeCallFunction(String arg);

}

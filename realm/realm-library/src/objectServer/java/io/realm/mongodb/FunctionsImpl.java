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

import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.functions.Functions;

/**
 * Internal implementation of Functions invoking the actual OS function in the context of the
 * {@link RealmUser}/{@link RealmApp}.
 */
class FunctionsImpl extends Functions {

    FunctionsImpl(RealmUser user) {
        this(user, user.getApp().getConfiguration().getDefaultCodecRegistry());
    }

    FunctionsImpl(RealmUser user, CodecRegistry codecRegistry) {
        super(user, codecRegistry);
    }

    // Invokes actual MongoDB Realm Function in the context of the associated user/app.
    @Override
    public <T> T invoke(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry) {
        Util.checkEmpty(name, "name");

        String encodedArgs = JniBsonProtocol.encode(args, codecRegistry);

        // NativePO calling scheme is actually synchronous
        AtomicReference<String> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<String> callback = new OsJNIResultCallback<String>(success, error) {
            @Override
            protected String mapSuccess(Object result) {
                return (String) result;
            }
        };
        nativeCallFunction(user.getApp().nativePtr, user.osUser.getNativePtr(), name, encodedArgs, callback);
        String encodedResponse = ResultHandler.handleResult(success, error);
        return JniBsonProtocol.decode(encodedResponse, resultClass, codecRegistry);
   }

   private static native void nativeCallFunction(long nativeAppPtr, long nativeUserPtr, String name, String args_json, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

}

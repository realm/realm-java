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

import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;


/**
 * A <i>Realm functions<i> manager to call MongoDB Realm functions.
 */
// TODO Timeout is currently handled uniformly through OkHttpNetworkTransport configured through RealmAppConfig
public class RealmFunctions {

    // FIXME Review memory allocation
    private final RealmUser user;
    private CodecRegistry defaultCodecRegistry;


    // FIXME Doc
    RealmFunctions(RealmUser user, CodecRegistry codecRegistry) {
        this.user = user;
        this.defaultCodecRegistry = codecRegistry;
    }

    /**
     * Call a MongoDB Realm function synchronously.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function. Primitive Java types and their boxed
     *             equivalents (bool/Boolean, int/Integer, long/Long and String) are automatically
     *             converted to their {@link BsonValue} equivalents, while
     *             {@link BsonValue}s are left as is.
     * @return Result of the Stitch function.
     *
     * @throws IllegalArgumentException if any of the arguments could not be converted to
     * {@link BsonValue}s
     * @throws ObjectServerError if the request failed in some way.
     * // FIXME Any other errors that we should expect
     */
    // FIXME Service?
    // FIXME Application wide invocation
    public <T> T callFunction(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry) {
        String encodedArgs = JniBsonProtocol.encode(args, codecRegistry);
        String encodedResponse = invoke(name, encodedArgs);
        return JniBsonProtocol.decode(encodedResponse, resultClass, codecRegistry);
    }

    /**
     * Call a MongoDB Realm function synchronously with typed result.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function. Java types like int/Integer, long/Long and
     *             String are automatically converted to the {@link BsonValue} equivalents, while
     *             {@link BsonValue}s are left as is.
     * @param clz  The type that the functions result should be converted to. If conversion is not
     *             possible a {@link IllegalArgumentException} is throwed.
     * @return Result of the Stitch function.
     *
     * @throws IllegalArgumentException if any of the arguments could not be converted to
     * {@link BsonValue}s or if the result could not be converted to the requested {@code clz}.
     * @throws ObjectServerError if the request failed in some way.
     * // FIXME Any other errors that we should expect
     */
    public <T> T callFunction(String name, List<?> args, Class<T> clz) {
        return callFunction(name, args, clz, defaultCodecRegistry);
    }

    /**
     * Call a Stitch function asynchronously.
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class, CodecRegistry)}.
     *
     * @param name Name of the MongoDB Realm function to call.
     * @param callback The callback to invoke on success
     * @param args Arguments to the MongoDB Realm function.
     * @return Result of the MongoDB Realm function.
     *
     * // FIXME How are object server errors propagated through the callback mechanism.
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     */
    // FIXME Evaluate original asynchronous Stitch API relying on Google Play Tasks. For now just
    //  use a RealmAsyncTask
    //  https://docs.mongodb.com/stitch-sdks/java/4/com/mongodb/stitch/android/core/services/StitchServiceClient.html
    //  <ResultT> Task<ResultT> callFunction​(String name, List<?> args, Long requestTimeout, Class<ResultT> resultClass, CodecRegistry codecRegistry);
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> clz, CodecRegistry codecRegistry, RealmApp.Callback<T> callback) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new RealmApp.Request<T>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public T run() throws ObjectServerError {
                return callFunction(name, args, clz, codecRegistry);
            }
        }.start();
    }

    // FIXME Doc
    // FIXME Evaluate how type conversion exceptions in async call are acting
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> clz, RealmApp.Callback<T> callback) {
        return callFunctionAsync(name, args, clz, defaultCodecRegistry, callback);
    }

    private String invoke(String name, String args) {
        // Native calling scheme is actually synchronous
        Util.checkEmpty(name, "name");
        Util.checkEmpty(args, "args");
        AtomicReference<String> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        OsJNIResultCallback<String> callback = new OsJNIResultCallback<String>(success, error) {
            @Override
            protected String mapSuccess(Object result) {
                return (String) result;
            }
        };
        nativeCallFunction(user.getApp().nativePtr, user.osUser.getNativePtr(), name, args, callback);
        return RealmApp.handleResult(success, error);
   }

   private static native void nativeCallFunction(long nativeAppPtr, long nativeUserPtr, String name, String args_json, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

}

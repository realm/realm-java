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
 *
 * Arguments and results are encoded/decoded with the <i>Realm functions'</i> codec registry either
 * inherited from the {@link RealmAppConfiguration#getDefaultCodecRegistry()} or set explicitly
 * when creating the <i>Realm functions</i>-instance through {@link RealmUser#getFunctions(CodecRegistry)}
 * or through the individual calls to {@link #callFunction(String, List, Class, CodecRegistry)}.
 */
// TODO Timeout is currently handled uniformly through OkHttpNetworkTransport configured through RealmAppConfig
// FIXME Application wide invocation; like StitchAppClient appClient = Stitch.getDefaultAppClient();
// FIXME Encoder/Decoder specific variants
// FIXME Service?
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
     * Call a MongoDB Realm function synchronously with custom codec registry encoding/decoding
     * arguments/results.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param codecRegistry Codec registry to use for argument encoding and result decoding.
     * @param <T> The type that the response will be decoded as using the {@code codecRegistry}.
     * @return Result of the Stitch function.
     *
     * @throws org.bson.codecs.configuration.CodecConfigurationException if not able to encode the
     * arguments or decode the response into the requested type.
     * @throws ObjectServerError if the request failed in some way.
     *
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, RealmApp.Callback)
     * @see RealmAppConfiguration#getDefaultCodecRegistry()
     */
    public <T> T callFunction(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry) {
        String encodedArgs = JniBsonProtocol.encode(args, codecRegistry);
        String encodedResponse = invoke(name, encodedArgs);
        return JniBsonProtocol.decode(encodedResponse, resultClass, codecRegistry);
    }

    /**
     * Call a MongoDB Realm function synchronously with default codec registry encoding/decoding
     * arguments/results.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param <T> The type that the response will be decoded as using the default codec registry.
     * @return Result of the Stitch function.
     *
     * @throws org.bson.codecs.configuration.CodecConfigurationException if not able to encode the
     * arguments or decode the response into the requested type.
     * @throws ObjectServerError if the request failed in some way.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     * @see RealmAppConfiguration#getDefaultCodecRegistry()
     */
    public <T> T callFunction(String name, List<?> args, Class<T> resultClass) {
        return callFunction(name, args, resultClass, defaultCodecRegistry);
    }

    /**
     * Call a MongoDB Realm function asynchronously with custom codec registry for encoding/decoding
     * arguments/results.
     *
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class, CodecRegistry)}.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param codecRegistry Codec registry to use for argument encoding and result decoding.
     * @param callback The callback that will receive the result of the request. It failing to
     *                 encode the argument/decode the result or call the remote function the result
     *                 will indicate the error as a {@link org.bson.codecs.configuration.CodecConfigurationException}
     *                 or {@link ObjectServerError} respectively.
     * @param <T> The type that the response will be decoded as using the default codec registry.
     * @return Result of the Stitch function.
     *
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, RealmApp.Callback)
     * @see RealmAppConfiguration#getDefaultCodecRegistry()
     */
    // FIXME Evaluate original asynchronous Stitch API relying on Google Play Tasks. For now just
    //  use a RealmAsyncTask
    //  https://docs.mongodb.com/stitch-sdks/java/4/com/mongodb/stitch/android/core/services/StitchServiceClient.html
    //  <ResultT> Task<ResultT> callFunction​(String name, List<?> args, Long requestTimeout, Class<ResultT> resultClass, CodecRegistry codecRegistry);
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry, RealmApp.Callback<T> callback) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new RealmApp.Request<T>(RealmApp.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public T run() throws ObjectServerError {
                return callFunction(name, args, resultClass, codecRegistry);
            }
        }.start();
    }

    /**
     * Call a MongoDB Realm function asynchronously with custom codec registry for encoding/decoding
     * arguments/results.
     *
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class)}.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param callback The callback that will receive the result of the request. It failing to
     *                 encode the argument/decode the result or call the remote function the result
     *                 will indicate the error as a {@link org.bson.codecs.configuration.CodecConfigurationException}
     *                 or {@link ObjectServerError} respectively.
     * @param <T> The type that the response will be decoded as using the default codec registry.
     * @return Result of the Stitch function.
     *
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class)
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, RealmApp.Callback)
     * @see RealmAppConfiguration#getDefaultCodecRegistry()
     */
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> resultClass, RealmApp.Callback<T> callback) {
        return callFunctionAsync(name, args, resultClass, defaultCodecRegistry, callback);
    }

    /**
     * Returns the default codec registry used for encoding arguments and decoding results for this
     * <i>Realm functions</i> instance.
     *
     * @return The default codec registry.
     */
    public CodecRegistry getDefaultCodecRegistry() {
        return defaultCodecRegistry;
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

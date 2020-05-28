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

package io.realm.mongodb.functions;

import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

import io.realm.ObjectServerError;
import io.realm.RealmApp;
import io.realm.RealmAppConfiguration;
import io.realm.RealmAsyncTask;
import io.realm.RealmUser;
import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;

/**
 * A <i>Functions<i> manager to call MongoDB Realm functions.
 * <p>
 * Arguments and results are encoded/decoded with the <i>Functions'</i> codec registry either
 * inherited from the {@link RealmAppConfiguration#getDefaultCodecRegistry()} or set explicitly
 * when creating the <i>Functions</i>-instance through {@link RealmUser#getFunctions(CodecRegistry)}
 * or through the individual calls to {@link #callFunction(String, List, Class, CodecRegistry)}.
 *
 * @see RealmUser#getFunctions()
 * @see RealmUser#getFunctions(CodecRegistry)
 * @see RealmApp#getFunctions(RealmUser)
 * @see RealmApp#getFunctions(RealmUser, CodecRegistry)
 * @see RealmAppConfiguration
 * @see CodecRegistry
 */
public abstract class Functions {

    protected RealmUser user;

    private CodecRegistry defaultCodecRegistry;

    protected Functions(RealmUser user, CodecRegistry codecRegistry) {
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
     * @throws ObjectServerError if the request failed in some way.
     * @throws org.bson.codecs.configuration.CodecConfigurationException if the {@code codecRegistry}
     * does not provide codecs for the argument or {@code resultClass}.
     * @throws org.bson.BSONException is an error occurred during BSON processing.
     *
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, RealmApp.Callback)
     * @see RealmAppConfiguration#getDefaultCodecRegistry()
     */
    public <T> T callFunction(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry) {
        return invoke(name, args, resultClass, codecRegistry);
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
     * @throws ObjectServerError if the request failed in some way.
     * @throws org.bson.codecs.configuration.CodecConfigurationException if the {@code codecRegistry}
     * does not provide codecs for the argument or {@code resultClass}.
     * @throws org.bson.BSONException is an error occurred during BSON processing.
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
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class, CodecRegistry)}.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param codecRegistry Codec registry to use for argument encoding and result decoding.
     * @param callback The callback that will receive the result of the request. If the request
     *                 failed in some way, the codec registry failed to provide codecs for the
     *                 arguments or {@code resultClass}, or an error occurres during BSON processing
     *                 the result will indicate the error as a {@link ObjectServerError},
     *                 {@link org.bson.codecs.configuration.CodecConfigurationException}
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
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class)}.
     *
     * @param name Name of the Stitch function to call.
     * @param args Arguments to the Stitch function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param callback The callback that will receive the result of the request. If the request
     *                 failed in some way, the codec registry failed to provide codecs for the
     *                 arguments or {@code resultClass}, or an error occurres during BSON processing
     *                 the result will indicate the error as a {@link ObjectServerError},
     *                 {@link org.bson.codecs.configuration.CodecConfigurationException}
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

    /**
     * Returns the {@link RealmApp} that this instance in associated with.
     *
     * @return The {@link RealmApp} that this instance in associated with.
     */
    public RealmApp getApp() {
        return user.getApp();
    }

    /**
     * Returns the {@link RealmUser} that this instance in associated with.
     *
     * @return The {@link RealmUser} that this instance in associated with.
     */
    public RealmUser getUser() {
        return user;
    }

    protected abstract <T> T invoke(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry);

}

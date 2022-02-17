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

import org.bson.codecs.Decoder;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

import io.realm.RealmAsyncTask;
import io.realm.annotations.Beta;
import io.realm.internal.Util;
import io.realm.internal.jni.JniBsonProtocol;
import io.realm.internal.mongodb.Request;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.AppException;
import io.realm.mongodb.User;

/**
 * A <i>Functions</i> manager to call remote Realm functions for the associated Realm App.
 * <p>
 * Arguments and results are encoded/decoded with the <i>Functions'</i> codec registry either
 * inherited from the {@link AppConfiguration#getDefaultCodecRegistry()} or set explicitly
 * when creating the <i>Functions</i>-instance through {@link User#getFunctions(CodecRegistry)}
 * or through the individual calls to {@link #callFunction(String, List, Class, CodecRegistry)}.
 *
 * @see User#getFunctions()
 * @see User#getFunctions(CodecRegistry)
 * @see App#getFunctions(User)
 * @see App#getFunctions(User, CodecRegistry)
 * @see AppConfiguration
 * @see CodecRegistry
 */
@Beta
public abstract class Functions {

    protected User user;

    private CodecRegistry defaultCodecRegistry;

    protected Functions(User user, CodecRegistry codecRegistry) {
        this.user = user;
        this.defaultCodecRegistry = codecRegistry;
    }

    /**
     * Call a MongoDB Realm function synchronously with custom codec registry encoding/decoding
     * arguments/results.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param codecRegistry Codec registry to use for argument encoding and result decoding.
     * @param <ResultT> The type that the response will be decoded as using the {@code codecRegistry}.
     * @return Result of the Realm function.
     *
     * @throws AppException if the request failed in some way.
     *
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, App.Callback)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <ResultT> ResultT callFunction(String name, List<?> args, Class<ResultT> resultClass, CodecRegistry codecRegistry) {
        return invoke(name, args, codecRegistry, JniBsonProtocol.getCodec(resultClass, codecRegistry));
    }

    /**
     * Call a MongoDB Realm function synchronously with default codec registry encoding/decoding
     * arguments/results.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param <ResultT> The type that the response will be decoded as using the default codec registry.
     * @return Result of the Realm function.
     *
     * @throws AppException if the request failed in some way.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <ResultT> ResultT callFunction(String name, List<?> args, Class<ResultT> resultClass) {
        return callFunction(name, args, resultClass, defaultCodecRegistry);
    }

    /**
     * Call a MongoDB Realm function synchronously with custom result decoder.
     * <p>
     * The arguments will be encoded with the default codec registry encoding.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultDecoder The decoder used to decode the result.
     * @param <ResultT> The type that the response will be decoded as using the {@code resultDecoder}
     * @return Result of the Realm function.
     *
     * @throws AppException if the request failed in some way.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <ResultT> ResultT callFunction(String name, List<?> args, Decoder<ResultT> resultDecoder) {
        return invoke(name, args, defaultCodecRegistry, resultDecoder);
    }

    /**
     * Call a MongoDB Realm function asynchronously with custom codec registry for encoding/decoding
     * arguments/results.
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class, CodecRegistry)}.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param codecRegistry Codec registry to use for argument encoding and result decoding.
     * @param callback The callback that will receive the result or any errors from the request.
     * @param <T> The type that the response will be decoded as using the default codec registry.
     * @return The task representing the ongoing operation.
     *
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class, CodecRegistry)
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, App.Callback)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> resultClass, CodecRegistry codecRegistry, App.Callback<T> callback) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new Request<T>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public T run() throws AppException {
                Decoder<T> decoder = JniBsonProtocol.getCodec(resultClass, codecRegistry);
                return invoke(name, args, codecRegistry, decoder);
            }
        }.start();
    }

    /**
     * Call a MongoDB Realm function asynchronously with custom codec registry for encoding/decoding
     * arguments/results.
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Class)}.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultClass  The type that the functions result should be converted to.
     * @param callback The callback that will receive the result or any errors from the request.
     * @param <T> The type that the response will be decoded as using the default codec registry.
     * @return The task representing the ongoing operation.
     *
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class)
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, App.Callback)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Class<T> resultClass, App.Callback<T> callback) {
        return callFunctionAsync(name, args, resultClass, defaultCodecRegistry, callback);
    }

    /**
     * Call a MongoDB Realm function asynchronously with custom result decoder.
     * <p>
     * This is the asynchronous equivalent of {@link #callFunction(String, List, Decoder)}.
     *
     * @param name Name of the Realm function to call.
     * @param args Arguments to the Realm function.
     * @param resultDecoder The decoder used to decode the result.
     * @param callback The callback that will receive the result or any errors from the request.
     * @param <T> The type that the response will be decoded as using the {@code resultDecoder}
     * @return The task representing the ongoing operation.
     *
     * @throws IllegalStateException if not called on a looper thread.
     *
     * @see #callFunction(String, List, Class)
     * @see #callFunctionAsync(String, List, Class, CodecRegistry, App.Callback)
     * @see AppConfiguration#getDefaultCodecRegistry()
     */
    public <T> RealmAsyncTask callFunctionAsync(String name, List<?> args, Decoder<T> resultDecoder, App.Callback<T> callback) {
        Util.checkLooperThread("Asynchronous functions is only possible from looper threads.");
        return new Request<T>(App.NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public T run() throws AppException {
                return invoke(name, args, defaultCodecRegistry, resultDecoder);
            }
        }.start();
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
     * Returns the {@link App} that this instance in associated with.
     *
     * @return The {@link App} that this instance in associated with.
     */
    public App getApp() {
        return user.getApp();
    }

    /**
     * Returns the {@link User} that this instance in associated with.
     *
     * @return The {@link User} that this instance in associated with.
     */
    public User getUser() {
        return user;
    }

    protected abstract <T> T invoke(String name, List<?> args, CodecRegistry codecRegistry, Decoder<T> resultDecoder);

}

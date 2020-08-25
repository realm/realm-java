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
package io.realm.mongodb.auth;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.annotations.Beta;
import io.realm.internal.mongodb.Request;
import io.realm.mongodb.AppException;
import io.realm.RealmAsyncTask;
import io.realm.internal.network.ResultHandler;
import io.realm.internal.Util;
import io.realm.internal.jni.OsJNIResultCallback;
import io.realm.internal.jni.OsJNIVoidResultCallback;
import io.realm.internal.objectstore.OsJavaNetworkTransport;
import io.realm.mongodb.App;
import io.realm.mongodb.User;

import static io.realm.mongodb.App.NETWORK_POOL_EXECUTOR;

/**
 * This class exposes functionality for a user to manage API keys under their control.
 */
@Beta
public abstract class ApiKeyAuth {

    private static final int TYPE_CREATE = 1;
    private static final int TYPE_FETCH_SINGLE = 2;
    private static final int TYPE_FETCH_ALL = 3;
    private static final int TYPE_DELETE = 4;
    private static final int TYPE_DISABLE = 5;
    private static final int TYPE_ENABLE = 6;

    private final User user;

    /**
     * Create an instance of this class for a specific user.
     *
     * @param user user that is controlling the API keys.
     */
    protected ApiKeyAuth(User user) {
        this.user = user;
    }

    /**
     * Returns the {@link User} that this instance in associated with.
     *
     * @return The {@link User} that this instance in associated with.
     */
    public User getUser() {
        return user;
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
     * Creates a user API key that can be used to authenticate as the user.
     * <p>
     * The value of the key must be persisted at this time as this is the only time it is visible.
     * <p>
     * The key is enabled when created. It can be disabled by calling {@link #disableApiKey(ObjectId)}.
     *
     * @param name the name of the key
     * @throws AppException if the server failed to create the API key.
     * @return the new API key for the user.
     */
    public UserApiKey createApiKey(String name) throws AppException {
        Util.checkEmpty(name, "name");
        AtomicReference<UserApiKey> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        OsJNIResultCallback<UserApiKey> callback = new OsJNIResultCallback<UserApiKey>(success, error) {
            @Override
            protected UserApiKey mapSuccess(Object result) {
                return createKeyFromNative((Object[]) result);
            }
        };
        call(TYPE_CREATE, name, callback);
        return ResultHandler.handleResult(success, error);
    }

    /**
     * Asynchronously creates a user API key that can be used to authenticate as the user.
     * <p>
     * The value of the key must be persisted at this time as this is the only time it is visible.
     * <p>
     * The key is enabled when created. It can be disabled by calling {@link #disableApiKey(ObjectId)}.
     *
     * @param name the name of the key
     * @param callback callback when key creation has completed or failed. The callback will always
     * happen on the same thread as this method is called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask createApiKeyAsync(String name, App.Callback<UserApiKey> callback) {
        Util.checkLooperThread("Asynchronous creation of api keys are only possible from looper threads.");
        return new Request<UserApiKey>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public UserApiKey run() throws AppException {
                return createApiKey(name);
            }
        }.start();
    }

    /**
     * Fetches a specific user API key associated with the user.
     *
     * @param id the id of the key to fetch.
     * @throws AppException if the server failed to fetch the API key.
     */
    public UserApiKey fetchApiKey(ObjectId id) throws AppException {
        Util.checkNull(id, "id");
        AtomicReference<UserApiKey> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_FETCH_SINGLE, id.toHexString(), new OsJNIResultCallback<UserApiKey>(success, error) {
            @Override
            protected UserApiKey mapSuccess(Object result) {
                return createKeyFromNative((Object[]) result);
            }
        });
        return ResultHandler.handleResult(success, error);
    }

    /**
     * Fetches a specific user API key associated with the user.
     *
     * @param id the id of the key to fetch.
     * @param callback callback used when the key was fetched or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask fetchApiKeyAsync(ObjectId id, App.Callback<UserApiKey> callback) {
        Util.checkLooperThread("Asynchronous fetching an api key is only possible from looper threads.");
        return new Request<UserApiKey>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public UserApiKey run() throws AppException {
                return fetchApiKey(id);
            }
        }.start();
    }

    /**
     * Fetches all API keys associated with the user.
     *
     * @throws AppException if the server failed to fetch the API keys.
     */
    public List<UserApiKey> fetchAllApiKeys() throws AppException {
        AtomicReference<List<UserApiKey>> success = new AtomicReference<>(null);
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_FETCH_ALL, null, new OsJNIResultCallback<List<UserApiKey>>(success, error) {
            @Override
            protected List<UserApiKey> mapSuccess(Object result) {
                Object[] keyData = (Object[]) result;
                List<UserApiKey> list = new ArrayList<>();
                for (int i = 0; i < keyData.length; i++) {
                    list.add(createKeyFromNative((Object[]) keyData[i]));
                }
                return list;
            }
        });
        return ResultHandler.handleResult(success, error);
    }


    /**
     * Fetches all API keys associated with the user.
     *
     * @param callback callback used when the keys were fetched or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask fetchAllApiKeys(App.Callback<List<UserApiKey>> callback) {
        Util.checkLooperThread("Asynchronous fetching an api key is only possible from looper threads.");
        return new Request<List<UserApiKey>>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public List<UserApiKey> run() throws AppException {
                return fetchAllApiKeys();
            }
        }.start();
    }

    /**
     * Deletes a specific API key created by the user.
     *
     * @param id the id of the key to delete.
     * @throws AppException if the server failed to delete the API key.
     */
    public void deleteApiKey(ObjectId id) throws AppException {
        Util.checkNull(id, "id");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_DELETE, id.toHexString(), new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    /**
     * Deletes a specific API key created by the user.
     *
     * @param id the id of the key to delete.
     * @param callback callback used when the was deleted or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask deleteApiKeyAsync(ObjectId id, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous deleting an api key is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                deleteApiKey(id);
                return null;
            }
        }.start();
    }

    /**
     * Disables a specific API key created by the user.
     *
     * @param id the id of the key to disable.
     * @throws AppException if the server failed to disable the API key.
     */
    public void disableApiKey(ObjectId id) throws AppException {
        Util.checkNull(id, "id");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_DISABLE, id.toHexString(), new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    /**
     * Disables a specific API key created by the user.
     *
     * @param id the id of the key to disable.
     * @param callback callback used when the key was disabled or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask disableApiKeyAsync(ObjectId id, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous disabling an api key is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                disableApiKey(id);
                return null;
            }
        }.start();
    }

    /**
     * Enables a specific API key created by the user.
     *
     * @param id the id of the key to enable.
     * @throws AppException if the server failed to enable the API key.
     */
    public void enableApiKey(ObjectId id) throws AppException {
        Util.checkNull(id, "id");
        AtomicReference<AppException> error = new AtomicReference<>(null);
        call(TYPE_ENABLE, id.toHexString(), new OsJNIVoidResultCallback(error));
        ResultHandler.handleResult(null, error);
    }

    /**
     * Enables a specific API key created by the user.
     *
     * @param id the id of the key to enable.
     * @param callback callback used when the key was enabled or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask enableApiKeyAsync(ObjectId id, App.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous enabling an api key is only possible from looper threads.");
        return new Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws AppException {
                enableApiKey(id);
                return null;
            }
        }.start();
    }

    private UserApiKey createKeyFromNative(Object[] keyData) {
        return new UserApiKey(new ObjectId((String) keyData[0]),
                (String) keyData[1],
                (String) keyData[2],
                !(Boolean) keyData[3]); // Server returns disabled state instead of enabled
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApiKeyAuth that = (ApiKeyAuth) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return "ApiKeyAuthProvider{" +
                "user=" + user.getId() +
                '}';
    }

    protected abstract void call(int functionType, @Nullable String arg, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);

}

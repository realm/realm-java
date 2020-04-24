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

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import io.realm.internal.Util;
import io.realm.internal.objectstore.OsJavaNetworkTransport;

import static io.realm.RealmApp.NETWORK_POOL_EXECUTOR;

/**
 * This class exposes functionality for a user to manage API keys under their control.
 */
public class ApiKeyAuth {

    private static final int TYPE_CREATE = 1;
    private static final int TYPE_FETCH_SINGLE = 2;
    private static final int TYPE_FETCH_ALL = 3;
    private static final int TYPE_DELETE = 4;
    private static final int TYPE_DISABLE = 5;
    private static final int TYPE_ENABLE = 6;

    private final RealmUser user;

    /**
     * Create an instance of this class for a specific user.
     *
     * @param user user that is controlling the API keys.
     */
    public ApiKeyAuth(RealmUser user) {
        this.user = user;
    }

    public RealmUser getUser() {
        return user;
    }

    public RealmApp getApp() {
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
     * @throws ObjectServer if the server failed to create the API key.
     * @return the new API key for the user.
     */
    public RealmUserApiKey createApiKey(String name) throws ObjectServerError {
        Util.checkEmpty(name, "name");
        AtomicReference<RealmUserApiKey> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        RealmApp.OsJNIResultCallback<RealmUserApiKey> callback = new RealmApp.OsJNIResultCallback<RealmUserApiKey>(success, error) {
            @Override
            protected RealmUserApiKey mapSuccess(Object result) {
                return createKeyFromNative((Object[]) result);
            }
        };
        nativeCallFunction(TYPE_CREATE, user.getApp().nativePtr, user.osUser.getNativePtr(), name, callback);
        return RealmApp.handleResult(success, error);
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
    public RealmAsyncTask createApiKeyAsync(String name, RealmApp.Callback<RealmUserApiKey> callback) {
        Util.checkLooperThread("Asynchronous creation of api keys are only possible from looper threads.");
        return new RealmApp.Request<RealmUserApiKey>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUserApiKey run() throws ObjectServerError {
                return createApiKey(name);
            }
        }.start();
    }

    /**
     * Fetches a specific user API key associated with the user.
     *
     * @param id the id of the key to fetch.
     * @throws ObjectServer if the server failed to fetch the API key.
     */
    public RealmUserApiKey fetchApiKey(ObjectId id) throws ObjectServerError {
        Util.checkNull(id, "id");
        AtomicReference<RealmUserApiKey> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_FETCH_SINGLE, user.getApp().nativePtr, user.osUser.getNativePtr(), id.toHexString(), new RealmApp.OsJNIResultCallback<RealmUserApiKey>(success, error) {
            @Override
            protected RealmUserApiKey mapSuccess(Object result) {
                return createKeyFromNative((Object[]) result);
            }
        });
        return RealmApp.handleResult(success, error);
    }

    /**
     * Fetches a specific user API key associated with the user.
     *
     * @param id the id of the key to fetch.
     * @param callback callback used when the key was fetched or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask fetchApiKeyAsync(ObjectId id, RealmApp.Callback<RealmUserApiKey> callback) {
        Util.checkLooperThread("Asynchronous fetching an api key is only possible from looper threads.");
        return new RealmApp.Request<RealmUserApiKey>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public RealmUserApiKey run() throws ObjectServerError {
                return fetchApiKey(id);
            }
        }.start();
    }

    /**
     * Fetches all API keys associated with the user.
     *
     * @throws ObjectServer if the server failed to fetch the API keys.
     */
    public List<RealmUserApiKey> fetchAllApiKeys() throws ObjectServerError {
        AtomicReference<List<RealmUserApiKey>> success = new AtomicReference<>(null);
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_FETCH_ALL, user.getApp().nativePtr, user.osUser.getNativePtr(), null, new RealmApp.OsJNIResultCallback<List<RealmUserApiKey>>(success, error) {
            @Override
            protected List<RealmUserApiKey> mapSuccess(Object result) {
                Object[] keyData = (Object[]) result;
                List<RealmUserApiKey> list = new ArrayList<>();
                for (int i = 0; i < keyData.length; i++) {
                    list.add(createKeyFromNative((Object[]) keyData[i]));
                }
                return list;
            }
        });
        return RealmApp.handleResult(success, error);
    }


    /**
     * Fetches all API keys associated with the user.
     *
     * @param callback callback used when the keys were fetched or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask fetchAllApiKeys(RealmApp.Callback<List<RealmUserApiKey>> callback) {
        Util.checkLooperThread("Asynchronous fetching an api key is only possible from looper threads.");
        return new RealmApp.Request<List<RealmUserApiKey>>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public List<RealmUserApiKey> run() throws ObjectServerError {
                return fetchAllApiKeys();
            }
        }.start();
    }

    /**
     * Deletes a specific API key created by the user.
     *
     * @param id the id of the key to delete.
     * @throws ObjectServer if the server failed to delete the API key.
     */
    public void deleteApiKey(ObjectId id) throws ObjectServerError {
        Util.checkNull(id, "id");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_DELETE, user.getApp().nativePtr, user.osUser.getNativePtr(), id.toHexString(), new RealmApp.OsJNIVoidResultCallback(error));
        RealmApp.handleResult(null, error);
    }

    /**
     * Deletes a specific API key created by the user.
     *
     * @param id the id of the key to delete.
     * @param callback callback used when the was deleted or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask deleteApiKeyAsync(ObjectId id, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous deleting an api key is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                deleteApiKey(id);
                return null;
            }
        }.start();
    }

    /**
     * Disables a specific API key created by the user.
     *
     * @param id the id of the key to disable.
     * @throws ObjectServer if the server failed to disable the API key.
     */
    public void disableApiKey(ObjectId id) throws ObjectServerError {
        Util.checkNull(id, "id");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_DISABLE, user.getApp().nativePtr, user.osUser.getNativePtr(), id.toHexString(), new RealmApp.OsJNIVoidResultCallback(error));
        RealmApp.handleResult(null, error);
    }

    /**
     * Disables a specific API key created by the user.
     *
     * @param id the id of the key to disable.
     * @param callback callback used when the key was disabled or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask disableApiKeyAsync(ObjectId id, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous disabling an api key is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                disableApiKey(id);
                return null;
            }
        }.start();
    }

    /**
     * Enables a specific API key created by the user.
     *
     * @param id the id of the key to enable.
     * @throws ObjectServer if the server failed to enable the API key.
     */
    public void enableApiKey(ObjectId id) throws ObjectServerError {
        Util.checkNull(id, "id");
        AtomicReference<ObjectServerError> error = new AtomicReference<>(null);
        nativeCallFunction(TYPE_ENABLE, user.getApp().nativePtr, user.osUser.getNativePtr(), id.toHexString(), new RealmApp.OsJNIVoidResultCallback(error));
        RealmApp.handleResult(null, error);
    }

    /**
     * Enables a specific API key created by the user.
     *
     * @param id the id of the key to enable.
     * @param callback callback used when the key was enabled or the call failed. The callback
     * will always happen on the same thread as this method was called on.
     * @throws IllegalStateException if called from a non-looper thread.
     */
    public RealmAsyncTask enableApiKeyAsync(ObjectId id, RealmApp.Callback<Void> callback) {
        Util.checkLooperThread("Asynchronous enabling an api key is only possible from looper threads.");
        return new RealmApp.Request<Void>(NETWORK_POOL_EXECUTOR, callback) {
            @Override
            public Void run() throws ObjectServerError {
                enableApiKey(id);
                return null;
            }
        }.start();
    }

    private RealmUserApiKey createKeyFromNative(Object[] keyData) {
        return new RealmUserApiKey(new ObjectId((String) keyData[0]),
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

    private static native void nativeCallFunction(int functionType, long nativeAppPtr, long nativeUserPtr, @Nullable String arg, OsJavaNetworkTransport.NetworkTransportJNIResultCallback callback);
}

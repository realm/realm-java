/*
 * Copyright 2016 Realm Inc.
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

package io.realm.objectserver.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import io.realm.log.RealmLog;
import io.realm.objectserver.User;
import io.realm.objectserver.UserStore;

/**
 * A User Store backed by a SharedPreferences file.
 */
public class SharedPrefsUserStore implements UserStore {

    public static final Executor THREAD_POOL;
    private final SharedPreferences sp;
    private Handler handler = new Handler(Looper.getMainLooper());

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            THREAD_POOL = AsyncTask.THREAD_POOL_EXECUTOR;
        } else {
            throw new UnsupportedOperationException("FIXME: Not supported yet. Realm.asyncTaskExecutor must be public first");
            // THREAD_POOL = Realm.asyncTaskExecutor; // FIXME Do this better
        }
    }

    public SharedPrefsUserStore(Context context) {
        sp = context.getSharedPreferences("realm_object_server_users", Context.MODE_PRIVATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean save(String key, User user) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, user.toJson());
        return editor.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAsync(String key, User user) {
        saveASync(key, user, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveASync(final String key, final User user, final Callback callback) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                boolean success;
                Throwable error = null;
                try {
                    success = save(key, user);
                    if (!success) {
                        error = new RuntimeException("Could not save key");
                    }
                } catch (Exception e) {
                    success = false;
                    error = e;
                    RealmLog.error("Failed to save user", e);
                }
                if (callback != null) {
                    final boolean finalSuccess = success;
                    final Throwable finalError = error;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalSuccess) {
                                callback.onSuccess(user);
                            } else {
                                callback.onError(finalError);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User load(String key) {
        String userData = sp.getString(key, "");
        if (userData.equals("")) {
            return null;
        }
        return User.fromJson(userData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAsync(final String key, final Callback callback) {
        THREAD_POOL.execute(new Runnable() {
            @Override
            public void run() {
                User user = null;
                Throwable error = null;
                try {
                    user = load(key);
                    if (user == null) {
                        error = new RuntimeException("Could not load user:" + key);
                    }
                } catch (Exception e) {
                    error = e;
                    RealmLog.error("Failed to save user", e);
                }
                if (callback != null) {
                    final User finalUser = user;
                    final Throwable finalError = error;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalUser != null) {
                                callback.onSuccess(finalUser);
                            } else {
                                callback.onError(finalError);
                            }
                        }
                    });
                }
            }
        });
    }
}

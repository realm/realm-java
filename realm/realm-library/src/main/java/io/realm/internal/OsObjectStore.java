/*
 * Copyright 2017 Realm Inc.
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

package io.realm.internal;

import javax.annotation.Nullable;

import io.realm.RealmConfiguration;

/**
 * Java wrapper for methods in object_store.hpp.
 */
public class OsObjectStore {

    public final static long SCHEMA_NOT_VERSIONED = -1;

    /**
     * Sets the primary key field for the given class.
     * <p>
     * NOTE: The search index has to be added to the field before calling this method.
     *
     * @throws IllegalStateException if it is not in a transaction.
     * @throws IllegalStateException if the given class doesn't exist.
     * @throws IllegalStateException if the given field doesn't exist.
     * @throws IllegalStateException if the given field is not a valid type for primary key.
     * @throws IllegalStateException if there are duplicated values for the given field.
     */
    public static void setPrimaryKeyForObject(OsSharedRealm sharedRealm, String className,
                                              @Nullable String primaryKeyFieldName) {
        nativeSetPrimaryKeyForObject(sharedRealm.getNativePtr(), className, primaryKeyFieldName);
    }

    public static @Nullable String getPrimaryKeyForObject(OsSharedRealm sharedRealm, String className) {
        return nativeGetPrimaryKeyForObject(sharedRealm.getNativePtr(), className);
    }

    /**
     * Sets the schema version to the given {@link OsSharedRealm}. This method will create meta tables if they don't exist.
     * @throws IllegalStateException if it is not in a transaction.
     */
    public static void setSchemaVersion(OsSharedRealm sharedRealm, long schemaVersion) {
        nativeSetSchemaVersion(sharedRealm.getNativePtr(), schemaVersion);
    }

    /**
     * Returns the schema version of the given {@link OsSharedRealm}. If meta tables don't exist, this will return
     * {@link #SCHEMA_NOT_VERSIONED}.
     */
    public static long getSchemaVersion(OsSharedRealm sharedRealm) {
        return nativeGetSchemaVersion(sharedRealm.getNativePtr());
    }

    /**
     * Deletes the table with the given class name.
     *
     * @return {@code true} if the table has been deleted. {@code false} if the table doesn't exist.
     * @throws IllegalStateException if it is not in a transaction.
     */
    public static boolean deleteTableForObject(OsSharedRealm sharedRealm, String className) {
        return nativeDeleteTableForObject(sharedRealm.getNativePtr(), className);
    }

    /**
     * Try to grab an exclusive lock on the given Realm file. If the lock can be acquired, the {@code runnable} will be
     * executed while the lock is held. The lock will ensure no one else can read from or write to the Realm file at the
     * same time.
     *
     * @param configuration to specify the realm path.
     * @param runnable to run with lock.
     * @return {@code true} if the lock can be acquired and the {@code runnable} has been executed.
     */
    public static boolean callWithLock(RealmConfiguration configuration, Runnable runnable) {
        return nativeCallWithLock(configuration.getPath(), runnable);
    }

    private native static void nativeSetPrimaryKeyForObject(long sharedRealmPtr, String className,
                                                             @Nullable String primaryKeyFieldName);

    private native static @Nullable String nativeGetPrimaryKeyForObject(long sharedRealmPtr, String className);

    private native static void nativeSetSchemaVersion(long sharedRealmPtr, long schemaVersion);

    private native static long nativeGetSchemaVersion(long sharedRealmPtr);

    private native static boolean nativeDeleteTableForObject(long sharedRealmPtr, String className);

    private native static boolean nativeCallWithLock(String realmPath, Runnable runnable);
}

/*
 * Copyright 2015 Realm Inc.
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
 *
 */

package io.realm.dynamic;

import android.os.Looper;

import java.util.HashMap;
import java.util.Map;

import io.realm.RealmConfiguration;
import io.realm.internal.SharedGroupManager;
import io.realm.internal.RealmBase;
import io.realm.internal.Table;

/**
 * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
 * done using Strings instead of classes.
 *
 * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file as both a dynamic Realm and the normal
 * typed one.
 *
 * Dynamic Realms do not enforce schemaVersions and doesn't trigger migrations, even though they have been defined in
 * the configuration.
 *
 * @see io.realm.Realm
 */
public class DynamicRealm extends RealmBase {

    // Cache mapping between a RealmConfiguration and already open Realm instances on this thread.
    protected static final ThreadLocal<Map<RealmConfiguration, DynamicRealm>> realmsCache =
            new ThreadLocal<Map<RealmConfiguration, DynamicRealm>>() {
                @Override
                protected Map<RealmConfiguration, DynamicRealm> initialValue() {
                    return new HashMap<RealmConfiguration, DynamicRealm>();
                }
            };

   private DynamicRealm(RealmConfiguration configuration, boolean autoRefresh) {
        super(configuration, autoRefresh);
    }

    /**
     * Realm static constructor that returns a dynamic variant of the Realm instance defined by provided
     * {@link io.realm.RealmConfiguration}. Dynamic Realms do not care about schemaVersion and schemas, so opening a
     * DynamicRealm will never trigger a migration
     *
     * @return The DynamicRealm defined by the configuration.
     * @see RealmConfiguration for details on how to configure a Realm.
     */
    public static DynamicRealm getInstance(RealmConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
        }
        return create(configuration);
    }

    private static synchronized DynamicRealm create(RealmConfiguration configuration) {

        boolean autoRefresh = Looper.myLooper() != null;

        // Check if a cached instance already exists for this thread
        String canonicalPath = configuration.getPath();
        Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
        Integer references = localRefCount.get(configuration);
        if (references == null) {
            references = 0;
        }

        Map<RealmConfiguration, DynamicRealm> realms = realmsCache.get();
        DynamicRealm realm = realms.get(configuration);
        if (realm != null) {
            localRefCount.put(configuration, references + 1);
            return realm;
        }

        // Create new Realm and cache it.
        validateAgainstExistingConfigurations(configuration);
        realm = new DynamicRealm(configuration, autoRefresh);
        realms.put(configuration, realm);
        localRefCount.put(configuration, references + 1);

        return realm;
    }

    /**
     * Closes the Realm instance and all its resources.
     * <p>
     * It's important to always remember to close Realm instances when you're done with it in order
     * not to leak memory, file descriptors or grow the size of Realm file out of measure.
     *
     * @throws java.lang.IllegalStateException if trying to close Realm on a different thread than the
     * one it was created on.
     */
    @Override
    public void close() {
        boolean wasLastInstance = closeInstance(configuration);
        if (wasLastInstance) {
            realmsCache.get().remove(configuration);
        }
    }

//    /**
//     * Returns a RealmQuery, which can be used to query for the provided class.
//     *
//     * @param clazz The class of the object which is to be queried for.
//     * @return A RealmQuery, which can be used to query for specific objects of provided type.
//     * @see io.realm.RealmQuery
//     */
//    public RealmQuery<DynamicRealmObject> where(String clazz) {
//        checkIfValid();
//        return new RealmQuery<DynamicRealmObject>(this, clazz);
//    }

    // Public because of migrations
    public Table getTable(String clazz) {
        return sharedGroup.getTable(clazz);
    }
}

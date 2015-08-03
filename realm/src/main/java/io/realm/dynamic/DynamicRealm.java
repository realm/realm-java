///*
// * Copyright 2015 Realm Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package io.realm.dynamic;
//
//import android.os.Looper;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import io.realm.Realm;
//import io.realm.RealmConfiguration;
//import io.realm.exceptions.RealmMigrationNeededException;
//import io.realm.internal.RealmInstance;
//
///**
// * DynamicRealm is a dynamic variant of {@link io.realm.Realm}. This means that all access to data and/or queries are
// * done using Strings instead of classes.
// *
// * The same {@link io.realm.RealmConfiguration} can be used to open a Realm file as both a dynamic Realm and the normal
// * typed one.
// *
// * Dynamic Realms do not enforce schemaVersions and doesn't trigger migrations, even though they have been defined in
// * the configuration.
// *
// * @see io.realm.Realm
// */
//public class DynamicRealm extends RealmInstance {
//
//   private DynamicRealm(RealmConfiguration configuration, boolean autoRefresh) {
//        super(configuration, autoRefresh);
//    }
//
//    /**
//     * Realm static constructor that returns a dynamic variant of the Realm instance defined by provided
//     * {@link io.realm.RealmConfiguration}. Dynamic Realms do not care about schemaVersion and schemas, so opening a
//     * DynamicRealm will never trigger a migration
//     *
//     * @return The DynamicRealm defined by the configuration.
//     * @see RealmConfiguration for details on how to configure a Realm.
//     */
//    public static DynamicRealm getInstance(RealmConfiguration configuration) {
//        if (configuration == null) {throw new IllegalArgumentException("A non-null RealmConfiguration must be provided");
//        }
//        return create(configuration);
//    }
//
//    private static synchronized DynamicRealm create(RealmConfiguration configuration) {
//        boolean autoRefresh = Looper.myLooper() != null;
//
//        // Check if a cached instance already exists for this thread
//        String canonicalPath = configuration.getPath();
//        Map<RealmConfiguration, Integer> localRefCount = referenceCount.get();
//        Integer references = localRefCount.get(configuration);
//        if (references == null) {
//            references = 0;
//        }
//        Map<RealmConfiguration, Realm> realms = realmsCache.get();
//        Realm realm = realms.get(configuration);
//        if (realm != null) {
//            localRefCount.put(configuration, references + 1);
//            return realm;
//        }
//
//
//        // Create new Realm and cache it. All exception code paths must close the Realm otherwise we risk serving
//        // faulty cache data.
//        validateAgainstExistingConfigurations(configuration);
//        realm = new Realm(configuration, autoRefresh);
//        realms.put(configuration, realm);
//        localRefCount.put(configuration, references + 1);
//
//        // Increment global reference counter
//        if (references == 0) {
//            AtomicInteger counter = globalOpenInstanceCounter.get(canonicalPath);
//            if (counter == null) {
//                globalOpenInstanceCounter.put(canonicalPath, new AtomicInteger(1));
//            } else {
//                counter.incrementAndGet();
//            }
//        }
//
//        // Check versions of Realm
//        long currentVersion = realm.getVersion();
//        long requiredVersion = configuration.getSchemaVersion();
//        if (currentVersion != UNVERSIONED && currentVersion < requiredVersion && validateSchema) {
//            realm.close();
//            throw new RealmMigrationNeededException(canonicalPath, String.format("Realm on disc need to migrate from v%s to v%s", currentVersion, requiredVersion));
//        }
//        if (currentVersion != UNVERSIONED && requiredVersion < currentVersion && validateSchema) {
//            realm.close();
//            throw new IllegalArgumentException(String.format("Realm on disc is newer than the one specified: v%s vs. v%s", currentVersion, requiredVersion));
//        }
//
//        // Initialize Realm schema if needed
//        if (validateSchema) {
//            try {
//                initializeRealm(realm);
//            } catch (RuntimeException e) {
//                realm.close();
//                throw e;
//            }
//        }
//
//        return realm;
//    }
//
//
//    @Override
//    public void close() throws IOException {
//
//    }
//}

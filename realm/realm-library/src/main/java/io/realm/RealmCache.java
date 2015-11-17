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
 */
package io.realm;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import io.realm.internal.ColumnIndices;
import io.realm.internal.RealmProxyMediator;
import io.realm.internal.SharedGroup;
import io.realm.internal.log.RealmLog;

/**
 * To cache {@link Realm}, {@link DynamicRealm} instances and related resources.
 * Every thread will share the same {@link Realm} and {@link DynamicRealm} instances which are referred to the same
 * {@link RealmConfiguration}.
 */
class RealmCache {

    private class RefAndCount {
        // The Realm instance in this thread.
        private final ThreadLocal<BaseRealm> localRealm = new ThreadLocal<>();
        // How many references to this Realm instance in this thread.
        private final ThreadLocal<Integer> localCount = new ThreadLocal<>();
        // How many threads have instances refer to this configuration.
        private int globalCount = 0;
    }
    private enum RealmCacheType {
        TYPED_REALM,
        DYNAMIC_REALM;

        static RealmCacheType valueOf(Class<? extends BaseRealm> cls) {
            if (cls == Realm.class) {
                return TYPED_REALM;
            } else if (cls == DynamicRealm.class) {
                return DYNAMIC_REALM;
            }

            throw new IllegalArgumentException("The type of Realm class must be Realm or DynamicRealm.");
        }
    }
    // Separated references and counters for typed Realm and dynamic Realm.
    private final EnumMap<RealmCacheType, RefAndCount> refAndCountMap;

    private RealmConfiguration configuration;
    // Column indices are cached to speed up opening typed Realm. If a Realm instance is created in one thread, creating
    // Realm instances in other threads doesn't have to initialize the column indices again.
    private ColumnIndices typedColumnIndices;

    // Different Realm configuration with same path are not allowed.
    private static Map<String, RealmCache> cachesMap = new HashMap<>();

    private static final String DIFFERENT_KEY_MESSAGE = "Wrong key used to decrypt Realm.";


    private RealmCache(RealmConfiguration config) {
        configuration = config;
        refAndCountMap = new EnumMap<>(RealmCacheType.class);
        for (RealmCacheType type : RealmCacheType.values()) {
            refAndCountMap.put(type, new RefAndCount());
        }
    }

    /**
     * Creates a new Realm instance or get a existing instance for current thread.
     *
     * @param configuration {@link RealmConfiguration} will be used to create or get the instance.
     * @param realmClass class of {@link Realm} or {@link DynamicRealm} to be created in or gotten from the cache.
     * @return the {@link Realm} or {@link DynamicRealm} instance.
     */
    static synchronized BaseRealm createRealmOrGetFromCache(RealmConfiguration configuration,
                                                        Class<? extends BaseRealm> realmClass) {
        boolean isCacheInMap = true;
        RealmCache cache = cachesMap.get(configuration.getPath());
        if (cache == null) {
            // Create a new cache
            cache = new RealmCache(configuration);
            // The new cache should be added to the map later
            isCacheInMap = false;
        } else {
            // != takes no time, check it first
            if (cache.configuration != configuration && cache.configuration.hashCode() != configuration.hashCode()) {
                // throw the exception
                validateAgainstExistingConfigurations(configuration, cache.configuration);
            }
        }

        RefAndCount refAndCount = cache.refAndCountMap.get(RealmCacheType.valueOf(realmClass));

        if (refAndCount.localRealm.get() == null) {
            // Create a new local Realm instance
            BaseRealm realm;

            if (realmClass == Realm.class) {
                // RealmMigrationNeededException might be thrown here
                realm = Realm.createInstance(configuration, cache.typedColumnIndices);
            } else {
                realm = DynamicRealm.createInstance(configuration);
            }
            // The Realm instance has been created without exceptions. Cache and reference count can be updated now.

            // The cache is not in the map yet. Add it to the map after the Realm instance created successfully.
            if (!isCacheInMap) {
                cachesMap.put(configuration.getPath(), cache);
            }
            refAndCount.localRealm.set(realm);
            refAndCount.localCount.set(0);
        }

        Integer refCount = refAndCount.localCount.get();
        if (refCount == 0) {
            if (realmClass == Realm.class && refAndCount.globalCount == 0) {
                cache.typedColumnIndices = refAndCount.localRealm.get().schema.columnIndices;
            }
            // This is the first instance in current thread, increase the global count.
            refAndCount.globalCount++;
        }
        refAndCount.localCount.set(refCount + 1);

        return refAndCount.localRealm.get();
    }

    /**
     * Releases a given {@link Realm} or {@link DynamicRealm} from cache. The instance won't be closed by this function.
     *
     * @param realm Realm instance to be released from cache.
     * @return local reference count to the type Realm after releasing. {@code -1} if the Realm instance is not in the
     *         cache.
     */
    static synchronized int release(BaseRealm realm) {
        String canonicalPath = realm.getPath();
        RealmCache cache = cachesMap.get(canonicalPath);
        Integer refCount = null;
        RefAndCount refAndCount = null;

        if (cache != null) {
            refAndCount = cache.refAndCountMap.get(RealmCacheType.valueOf(realm.getClass()));
            refCount = refAndCount.localCount.get();
        }
        if (refCount == null) {
            refCount = 0;
        }

        if (refCount <= 0) {
            RealmLog.w("Realm " + canonicalPath + " is not in the cache anymore.");
            return -1;
        }


        // Decrease the local counter.
        refCount -= 1;

        if (refCount == 0) {
            // The last instance in this thread.
            // Clear local ref & counter
            refAndCount.localCount.set(null);
            refAndCount.localRealm.set(null);

            // Clear global counter
            refAndCount.globalCount--;
            if (refAndCount.globalCount < 0) {
                // Should never happen.
                throw new IllegalStateException("Global reference counter of Realm" + canonicalPath +
                        " gets corrupted.");
            }

            // Clear the column indices cache if needed
            if (realm instanceof Realm && refAndCount.globalCount == 0) {
                // All typed Realm instances of this file are cleared from cache
                cache.typedColumnIndices = null;
            }

            int totalRefCount = 0;
            for (RealmCacheType type : RealmCacheType.values()) {
                totalRefCount += cache.refAndCountMap.get(type).globalCount;
            }
            // No more instance of typed Realm and dynamic Realm. Remove the configuration from cache.
            if (totalRefCount == 0) {
                cachesMap.remove(canonicalPath);
            }
        } else {
            refAndCount.localCount.set(refCount);
        }

        return refCount;
    }

    /**
     * Make sure that the new configuration doesn't clash with any existing configurations for the
     * Realm.
     *
     * @throws IllegalArgumentException If the new configuration isn't valid.
     */
    private static void validateAgainstExistingConfigurations(RealmConfiguration newConfiguration,
                                                                           RealmConfiguration cachedConfiguration) {
        // Check that encryption keys aren't different
        if (!Arrays.equals(cachedConfiguration.getEncryptionKey(), newConfiguration.getEncryptionKey())) {
            throw new IllegalArgumentException(DIFFERENT_KEY_MESSAGE);
        }

        // Check schema versions are the same
        if (cachedConfiguration.getSchemaVersion() != newConfiguration.getSchemaVersion()) {
            throw new IllegalArgumentException(String.format("Configurations cannot have different schema versions " +
                            "if used to open the same file. %d vs. %d", cachedConfiguration.getSchemaVersion(),
                    newConfiguration.getSchemaVersion()));
        }

        // Check that schema is the same
        RealmProxyMediator cachedSchema = cachedConfiguration.getSchemaMediator();
        RealmProxyMediator schema = newConfiguration.getSchemaMediator();
        if (!cachedSchema.equals(schema)) {
            throw new IllegalArgumentException("Two configurations with different schemas are trying to open " +
                    "the same Realm file. Their schema must be the same: " + newConfiguration.getPath());
        }

        // Check if the durability is the same
        SharedGroup.Durability cachedDurability = cachedConfiguration.getDurability();
        SharedGroup.Durability newDurability = newConfiguration.getDurability();
        if (!cachedDurability.equals(newDurability)) {
            throw new IllegalArgumentException("A Realm cannot be both in-memory and persisted. Two conflicting " +
                    "configurations pointing to " + newConfiguration.getPath() + " are being used.");
        }

        // Should never get here
        throw new IllegalArgumentException(
                "The new configuration pointing to " + newConfiguration.getPath() +
                        " has a different hash code with a cached configuration. " +
                        "The new configuration's hashcode: " + newConfiguration.hashCode() +
                        "The cached configuration's hashcode: " + cachedConfiguration.hashCode() + ".");
    }

    /**
     * Returns the total reference count of {@link Realm} and {@link DynamicRealm} refer to the given
     * {@link RealmConfiguration}.
     * <p>
     * This function is not thread safe. Consider to call this function and related logic in a {@code synchronized}
     * block on {@code RealmCache.class}.
     *
     * @param configuration the {@link RealmConfiguration} of {@link Realm} and {@link DynamicRealm}.
     * @return 0 if there is no {@link Realm} and {@link DynamicRealm} refer to the {@link RealmConfiguration} in the
     * cache.
     */
    static int getGlobalRefCount(RealmConfiguration configuration) {
        RealmCache cache = cachesMap.get(configuration.getPath());
        if (cache == null) {
            return 0;
        }
        int totalRefCount = 0;
        for (RealmCacheType type : RealmCacheType.values()) {
            totalRefCount += cache.refAndCountMap.get(type).globalCount;
        }
        return totalRefCount;
    }
}

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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.realm.internal.ManageableObject;
import io.realm.internal.OsMap;
import io.realm.internal.OsObjectStore;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.core.NativeMixed;

/**
 * A {@code ManagedMapManager} abstracts the different types of keys and values a managed
 * {@link RealmMap} can contain.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
abstract class ManagedMapManager<K, V> implements Map<K, V>, ManageableObject {

    protected final Class<K> keyClass;
    protected final MapValueOperator<V> mapValueOperator;

    ManagedMapManager(Class<K> keyClass, MapValueOperator<V> mapValueOperator) {
        this.keyClass = keyClass;
        this.mapValueOperator = mapValueOperator;
    }

    @Override
    public abstract V put(K key, V value);

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public boolean isValid() {
        return mapValueOperator.isValid();
    }

    @Override
    public boolean isFrozen() {
        return mapValueOperator.isFrozen();
    }

    @Override
    public int size() {
        return mapValueOperator.size();
    }

    @Override
    public boolean isEmpty() {
        return mapValueOperator.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO: use operator + do it natively
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO: use operator + do it natively
        return false;
    }

    @Override
    public void putAll(Map m) {
        // TODO: use operator + do it natively
    }

    @Override
    public void clear() {
        mapValueOperator.clear();
    }

    @Override
    public Set<K> keySet() {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public Collection<V> values() {
        // TODO: use operator + do it natively
        return null;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO: use operator + do it natively
        return null;
    }
}

/**
 * Specialization for {@link ManagedMapManager}s targeting {@link RealmDictionary}.
 * <p>
 * Dictionaries can in turn contain values of type {@link Mixed} and Realm primitive types, i.e.
 * integer, boolean, string, byte array, date, float, double, decimal, object id, UUID and
 * {@link RealmModel}.
 * <p>
 * A {@link MapValueOperator} representing the {@code V}-value type has to be used when
 * instantiating this operator.
 *
 * @param <V> the value type
 */
class DictionaryManager<V> extends ManagedMapManager<String, V> {

    DictionaryManager(MapValueOperator<V> mapValueOperator) {
        super(String.class, mapValueOperator);
    }

    @Override
    public V get(Object key) {
        return mapValueOperator.get(key);
    }

    @Override
    public V put(String key, V value) {
        return mapValueOperator.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V removedValue = mapValueOperator.get(key);
        mapValueOperator.remove(key);
        return removedValue;
    }
}

/**
 * Abstraction for different map value types. Here are defined as generics but specializations
 * should provide concrete types.
 *
 * @param <V> the value type
 */
abstract class MapValueOperator<V> {

    protected final BaseRealm baseRealm;
    protected final OsMap osMap;
    protected final ClassContainer classContainer;

    MapValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        this.baseRealm = baseRealm;
        this.osMap = osMap;
        this.classContainer = classContainer;
    }

    @Nullable
    public abstract V get(Object key);

    @Nullable
    public abstract V put(Object key, V value);

    public void remove(Object key) {
        osMap.remove(key);
    }

    public int size() {
        return (int) osMap.size();
    }

    public boolean isEmpty() {
        return osMap.size() == 0;
    }

    public boolean isValid() {
        return !baseRealm.isClosed();
    }

    public boolean isFrozen() {
        return baseRealm.isFrozen();
    }

    public void clear() {
        osMap.clear();
    }

    protected <E extends RealmModel> E copyToRealm(E object) {
        // TODO: support dynamic Realms
        // At this point the object can only be a typed object, so the backing Realm cannot be a DynamicRealm.
        Realm realm = (Realm) baseRealm;
        String simpleClassName = realm.getConfiguration().getSchemaMediator().getSimpleClassName(object.getClass());
        if (OsObjectStore.getPrimaryKeyForObject(realm.getSharedRealm(), simpleClassName) != null) {
            return realm.copyToRealmOrUpdate(object);
        } else {
            return realm.copyToRealm(object);
        }
    }
}

/**
 * {@link MapValueOperator} targeting {@link Mixed} values in {@link RealmMap}s.
 */
class MixedValueOperator extends MapValueOperator<Mixed> {

    MixedValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Override
    public Mixed get(Object key) {
        long mixedPtr = osMap.getMixedPtr(key);
        NativeMixed nativeMixed = new NativeMixed(mixedPtr);
        return (Mixed) osMap.get(key);
    }

    @Override
    public Mixed put(Object key, Mixed value) {
        Mixed original = (Mixed) osMap.get(key);
        osMap.put(key, value.getNativePtr());
        return original;
    }
}

/**
 * {@link MapValueOperator} targeting boxable values in {@link RealmMap}s.
 */
class BoxableValueOperator<T> extends MapValueOperator<T> {

    BoxableValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Nullable
    @Override
    public T get(Object key) {
        Object value = osMap.get(key);
        if (value == null) {
            return null;
        }
        return processValue(value);
    }

    @Nullable
    @Override
    public T put(Object key, T value) {
        T original = get(key);
        osMap.put(key, value);
        return original;
    }

    /**
     * Normally it is enough with typecasting the value to {@code T}, but e.g. {@link Long} cannot
     * be cast directly to {@link Integer} so a special operator has to override this method to do
     * it.
     *
     * @param value the value of the dictionary entry as an {@link Object}.
     * @return the value in its right form
     */
    @Nullable
    protected T processValue(Object value) {
        //noinspection unchecked
        return (T) value;
    }
}

/**
 * {@link MapValueOperator} targeting {@link Integer} values in {@link RealmMap}s. Use this one
 * instead of {@link BoxableValueOperator} to avoid and typecast exception when converting the
 * {@link Long} result from JNI to {@link Integer}.
 */
class IntegerValueOperator extends BoxableValueOperator<Integer> {

    IntegerValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Override
    protected Integer processValue(Object value) {
        return ((Long) value).intValue();
    }
}

/**
 * {@link MapValueOperator} targeting {@link RealmModel}s values in {@link RealmMap}s.
 */
class RealmModelValueOperator<T> extends MapValueOperator<T> {

    RealmModelValueOperator(BaseRealm baseRealm, OsMap osMap, ClassContainer classContainer) {
        super(baseRealm, osMap, classContainer);
    }

    @Nullable
    @Override
    public T get(Object key) {
        long realmModelKey = osMap.getModelRowKey(key);
        if (realmModelKey == -1) {
            return null;
        }

        //noinspection unchecked
        return (T) baseRealm.get((Class<? extends RealmModel>) classContainer.getClazz(), classContainer.getClassName(), realmModelKey);
    }

    @Nullable
    @Override
    public T put(Object key, @Nullable T value) {
        //noinspection unchecked
        Class<T> clazz = (Class<T>) classContainer.getClazz();
        String className = classContainer.getClassName();
        long rowModelKey = osMap.getModelRowKey(key);

        RealmModel realmObject = (RealmModel) value;
        boolean copyObject;
        if (value == null) {
            osMap.put(key, null);
        } else {
            copyObject = checkCanObjectBeCopied(baseRealm, realmObject, classContainer);
            RealmObjectProxy proxy = (RealmObjectProxy) ((copyObject) ? copyToRealm((RealmModel) value) : realmObject);
            osMap.putRow(key, proxy.realmGet$proxyState().getRow$realm().getObjectKey());
        }

        if (rowModelKey == -1) {
            return null;
        } else {
            //noinspection unchecked
            return (T) baseRealm.get((Class<? extends RealmModel>) clazz, className, rowModelKey);
        }
    }

    // TODO: unify this method and the one in RealmModelListOperator
    private boolean checkCanObjectBeCopied(BaseRealm realm, RealmModel object, ClassContainer classContainer) {
        if (object instanceof RealmObjectProxy) {
            RealmObjectProxy proxy = (RealmObjectProxy) object;

            if (proxy instanceof DynamicRealmObject) {
                if (classContainer.getClassName() == null) {
                    throw new IllegalStateException("A 'className' must be passed to the value operator when working with Dynamic Realms.");
                }

                @Nonnull
                String listClassName = classContainer.getClassName();
                if (proxy.realmGet$proxyState().getRealm$realm() == realm) {
                    String objectClassName = ((DynamicRealmObject) object).getType();
                    if (listClassName.equals(objectClassName)) {
                        // Same Realm instance and same target table
                        return false;
                    } else {
                        // Different target table
                        throw new IllegalArgumentException(String.format(Locale.US,
                                "The object has a different type from list's." +
                                        " Type of the list is '%s', type of object is '%s'.", listClassName, objectClassName));
                    }
                } else if (realm.threadId == proxy.realmGet$proxyState().getRealm$realm().threadId) {
                    // We don't support moving DynamicRealmObjects across Realms automatically. The overhead is too big as
                    // you have to run a full schema validation for each object.
                    // And copying from another Realm instance pointed to the same Realm file is not supported as well.
                    throw new IllegalArgumentException("Cannot copy DynamicRealmObject between Realm instances.");
                } else {
                    throw new IllegalStateException("Cannot copy an object to a Realm instance created in another thread.");
                }
            } else {
                // Object is already in this realm
                if (proxy.realmGet$proxyState().getRow$realm() != null && proxy.realmGet$proxyState().getRealm$realm().getPath().equals(realm.getPath())) {
                    if (realm != proxy.realmGet$proxyState().getRealm$realm()) {
                        throw new IllegalArgumentException("Cannot copy an object from another Realm instance.");
                    }
                    return false;
                }
            }
        }
        return true;
    }
}

/**
 * Used to avoid passing a {@link Class} and a {@link String} via parameters to the value operators.
 */
class ClassContainer {

    @Nullable
    private final Class<?> clazz;
    @Nullable
    private final String className;

    public ClassContainer(@Nullable Class<?> clazz, @Nullable String className) {
        this.clazz = clazz;
        this.className = className;
    }

    @Nullable
    public Class<?> getClazz() {
        return clazz;
    }

    @Nullable
    public String getClassName() {
        return className;
    }
}

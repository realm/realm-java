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

import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.realm.internal.OsMap;

/**
 * Specialization for {@link RealmMap}s whose keys are strings.
 * <p>
 * Similarly to {@link RealmList}s, a RealmDictionary can operate in managed and unmanaged modes. In
 * managed mode a RealmDictionary persists all its contents inside a Realm whereas in unmanaged mode
 * it functions like a {@link HashMap}.
 * <p>
 * Managed RealmDictionaries can only be created by Realm and will automatically update its content
 * whenever the underlying Realm is updated. Managed RealmDictionaries can only be accessed using
 * the getter that points to a RealmDictionary field of a {@link RealmObject}.
 * <p>
 * Unmanaged RealmDictionaries can be created by the user and can contain both managed and unmanaged
 * RealmObjects. This is useful when dealing with JSON deserializers like GSON or other frameworks
 * that inject values into a class. Unmanaged elements in this list can be added to a Realm using
 * the {@link Realm#copyToRealm(Iterable, ImportFlag...)} method.
 *
 * @param <V> the type of the values stored in this dictionary
 */
public class RealmDictionary<V> extends RealmMap<String, V> {

    // ------------------------------------------
    // Unmanaged constructors
    // ------------------------------------------

    /**
     * Instantiates a RealmDictionary in unmanaged mode.
     */
    public RealmDictionary() {
        super();
    }

    /**
     * Instantiates a RealmDictionary in unmanaged mode with an initial dictionary.
     *
     * @param dictionary initial dictionary
     */
    public RealmDictionary(RealmDictionary<V> dictionary) {
        super(dictionary.toMap());
    }

    // ------------------------------------------
    // Managed constructors
    // ------------------------------------------

    /**
     * Constructor used by {@code Realm}s.
     *
     * @param baseRealm
     * @param osMap
     * @param valueClass
     */
    RealmDictionary(BaseRealm baseRealm, OsMap osMap, Class<V> valueClass) {
        super(getStrategy(valueClass, baseRealm, osMap));
    }

    /**
     * Constructor used by {@code DynamicRealm}s.
     *
     * @param baseRealm
     * @param osMap
     * @param valueClass
     */
    RealmDictionary(BaseRealm baseRealm, OsMap osMap, String valueClass) {
        super(getStrategy(valueClass, baseRealm, osMap));
    }

    // ------------------------------------------
    // Private stuff
    // ------------------------------------------

    private Map<String, V> toMap() {
        Map<String, V> map = new HashMap<>();
        for (Entry<String, V> entry : this.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    private static <V extends RealmModel> LinkSelectorForMap<String, V> getRealmSelector(Class<V> valueClass,
                                                                                         BaseRealm baseRealm,
                                                                                         OsMap osMap) {
        return new LinkSelectorForMap<>(baseRealm, osMap, String.class, valueClass);
    }

    @SuppressWarnings("unchecked")
    private static <V> ManagedMapStrategy<String, V> getStrategy(Class<V> valueClass,
                                                                 BaseRealm baseRealm,
                                                                 OsMap osMap) {
        if (isClassForRealmModel(valueClass)) {
            Class<? extends RealmModel> typeCastClass = (Class<? extends RealmModel>) valueClass;
            TypeSelectorForMap<String, RealmModel> realmSelector = (TypeSelectorForMap<String, RealmModel>) getRealmSelector(typeCastClass, baseRealm, osMap);
            ManagedMapManager<String, RealmModel> dictionaryManager = new DictionaryManager<>(baseRealm,
                    new RealmModelValueOperator<>(baseRealm, osMap, realmSelector),
                    realmSelector);

            return (ManagedMapStrategy<String, V>) new ManagedMapStrategy<>(dictionaryManager);
        }

        DictionaryManager<V> manager = getManager(valueClass, baseRealm, osMap);
        return new ManagedMapStrategy<>(manager);
    }

    private static <V> ManagedMapStrategy<String, V> getStrategy(String valueClass,
                                                                 BaseRealm baseRealm,
                                                                 OsMap osMap) {
        DictionaryManager<V> manager = getManager(valueClass, baseRealm, osMap);
        return new ManagedMapStrategy<>(manager);
    }

    @SuppressWarnings("unchecked")
    private static <V> DictionaryManager<V> getManager(Class<V> valueClass,
                                                       BaseRealm baseRealm,
                                                       OsMap osMap) {
        TypeSelectorForMap<String, V> typeSelectorForMap = new SelectorForMap<>(baseRealm, osMap, String.class, valueClass);

        MapValueOperator<String, ?> mapValueOperator;

        if (valueClass == Mixed.class) {
            mapValueOperator = new MixedValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Mixed>) typeSelectorForMap);
        } else if (valueClass == Long.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Long.class, baseRealm, osMap, (TypeSelectorForMap<String, Long>) typeSelectorForMap, RealmMapEntrySet.IteratorType.LONG);
        } else if (valueClass == Float.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Float.class, baseRealm, osMap, (TypeSelectorForMap<String, Float>) typeSelectorForMap, RealmMapEntrySet.IteratorType.FLOAT);
        } else if (valueClass == Double.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Double.class, baseRealm, osMap, (TypeSelectorForMap<String, Double>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DOUBLE);
        } else if (valueClass == String.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(String.class, baseRealm, osMap, (TypeSelectorForMap<String, String>) typeSelectorForMap, RealmMapEntrySet.IteratorType.STRING);
        } else if (valueClass == Boolean.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Boolean.class, baseRealm, osMap, (TypeSelectorForMap<String, Boolean>) typeSelectorForMap, RealmMapEntrySet.IteratorType.BOOLEAN);
        } else if (valueClass == Date.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Date.class, baseRealm, osMap, (TypeSelectorForMap<String, Date>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DATE);
        } else if (valueClass == Decimal128.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Decimal128.class, baseRealm, osMap, (TypeSelectorForMap<String, Decimal128>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DECIMAL128);
        } else if (valueClass == Integer.class) {
            mapValueOperator = new IntegerValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Integer>) typeSelectorForMap);
        } else if (valueClass == Short.class) {
            mapValueOperator = new ShortValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Short>) typeSelectorForMap);
        } else if (valueClass == Byte.class) {
            mapValueOperator = new ByteValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Byte>) typeSelectorForMap);
        } else if (valueClass == byte[].class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>((Class<V>) byte[].class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.BINARY, (EqualsHelper<String, V>) new BinaryEquals<String>());
        } else if (valueClass == ObjectId.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(ObjectId.class, baseRealm, osMap, (TypeSelectorForMap<String, ObjectId>) typeSelectorForMap, RealmMapEntrySet.IteratorType.OBJECT_ID);
        } else if (valueClass == UUID.class) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(UUID.class, baseRealm, osMap, (TypeSelectorForMap<String, UUID>) typeSelectorForMap, RealmMapEntrySet.IteratorType.UUID);
        } else {
            throw new IllegalArgumentException("Only Maps of Mixed or one of the types that can be boxed inside Mixed can be used.");
        }

        return new DictionaryManager<>(baseRealm,
                (MapValueOperator<String, V>) mapValueOperator,
                typeSelectorForMap);
    }

    @SuppressWarnings("unchecked")
    private static <V> DictionaryManager<V> getManager(String valueClass,
                                                       BaseRealm baseRealm,
                                                       OsMap osMap) {
        TypeSelectorForMap<String, V> typeSelectorForMap = new DynamicSelectorForMap<>(baseRealm, osMap, valueClass);

        MapValueOperator<String, ?> mapValueOperator;

        if (valueClass.equals(Mixed.class.getCanonicalName())) {
            mapValueOperator = new MixedValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Mixed>) typeSelectorForMap);
        } else if (valueClass.equals(Long.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Long.class, baseRealm, osMap, (TypeSelectorForMap<String, Long>) typeSelectorForMap, RealmMapEntrySet.IteratorType.LONG);
        } else if (valueClass.equals(Float.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Float.class, baseRealm, osMap, (TypeSelectorForMap<String, Float>) typeSelectorForMap, RealmMapEntrySet.IteratorType.FLOAT);
        } else if (valueClass.equals(Double.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Double.class, baseRealm, osMap, (TypeSelectorForMap<String, Double>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DOUBLE);
        } else if (valueClass.equals(String.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(String.class, baseRealm, osMap, (TypeSelectorForMap<String, String>) typeSelectorForMap, RealmMapEntrySet.IteratorType.STRING);
        } else if (valueClass.equals(Boolean.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Boolean.class, baseRealm, osMap, (TypeSelectorForMap<String, Boolean>) typeSelectorForMap, RealmMapEntrySet.IteratorType.BOOLEAN);
        } else if (valueClass.equals(Date.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Date.class, baseRealm, osMap, (TypeSelectorForMap<String, Date>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DATE);
        } else if (valueClass.equals(Decimal128.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(Decimal128.class, baseRealm, osMap, (TypeSelectorForMap<String, Decimal128>) typeSelectorForMap, RealmMapEntrySet.IteratorType.DECIMAL128);
        } else if (valueClass.equals(Integer.class.getCanonicalName())) {
            mapValueOperator = new IntegerValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Integer>) typeSelectorForMap);
        } else if (valueClass.equals(Short.class.getCanonicalName())) {
            mapValueOperator = new ShortValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Short>) typeSelectorForMap);
        } else if (valueClass.equals(Byte.class.getCanonicalName())) {
            mapValueOperator = new ByteValueOperator<>(baseRealm, osMap, (TypeSelectorForMap<String, Byte>) typeSelectorForMap);
        } else if (valueClass.equals(byte[].class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>((Class<V>) byte[].class, baseRealm, osMap, typeSelectorForMap, RealmMapEntrySet.IteratorType.BINARY, (EqualsHelper<String, V>) new BinaryEquals<String>());
        } else if (valueClass.equals(ObjectId.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(ObjectId.class, baseRealm, osMap, (TypeSelectorForMap<String, ObjectId>) typeSelectorForMap, RealmMapEntrySet.IteratorType.OBJECT_ID);
        } else if (valueClass.equals(UUID.class.getCanonicalName())) {
            mapValueOperator = new GenericPrimitiveValueOperator<>(UUID.class, baseRealm, osMap, (TypeSelectorForMap<String, UUID>) typeSelectorForMap, RealmMapEntrySet.IteratorType.UUID);
        } else {
            throw new IllegalArgumentException("Only Maps of Mixed or one of the types that can be boxed inside Mixed can be used.");
        }

        return new DictionaryManager<>(baseRealm,
                (MapValueOperator<String, V>) mapValueOperator,
                typeSelectorForMap);
    }

    private static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }
}

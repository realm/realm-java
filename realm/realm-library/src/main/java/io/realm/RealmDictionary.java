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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    private static <V> ManagedMapStrategy<String, V> getStrategy(Class<V> valueClass, BaseRealm baseRealm, OsMap osMap) {
        if (isClassForRealmModel(valueClass)) {
            ClassContainer classContainer = new ClassContainer(valueClass, null);
            RealmModelValueOperator<RealmModel> realmModelValueOperator = new RealmModelValueOperator<>(baseRealm, osMap, classContainer);
            ManagedMapManager<String, RealmModel> dictionaryManager = new DictionaryManager<>(realmModelValueOperator);

            //noinspection unchecked
            return (ManagedMapStrategy<String, V>) new ManagedMapStrategy<>(dictionaryManager);
        }
        return getStrategy(valueClass.getCanonicalName(), baseRealm, osMap);
    }

    private static <V> ManagedMapStrategy<String, V> getStrategy(String valueClass, BaseRealm baseRealm, OsMap osMap) {
        DictionaryManager<V> manager = getManager(valueClass, baseRealm, osMap);
        return new ManagedMapStrategy<>(manager);
    }

    private static <V> DictionaryManager<V> getManager(String valueClass, BaseRealm baseRealm, OsMap osMap) {
        // TODO: add other types when ready
        ClassContainer classContainer = new ClassContainer(null, valueClass);

        MapValueOperator<?> mapValueOperator;

        if (isClassBoxable(valueClass)) {
            mapValueOperator = new BoxableValueOperator<>(baseRealm, osMap, classContainer);
        } else if (valueClass.equals(Mixed.class.getCanonicalName())) {
            mapValueOperator = new MixedValueOperator(baseRealm, osMap, classContainer);
        } else if (valueClass.equals(Integer.class.getCanonicalName())) {
            mapValueOperator = new IntegerValueOperator(baseRealm, osMap, classContainer);
        } else if (valueClass.equals(Short.class.getCanonicalName())) {
            mapValueOperator = new ShortValueOperator(baseRealm, osMap, classContainer);
        } else if (valueClass.equals(Byte.class.getCanonicalName())) {
            mapValueOperator = new ByteValueOperator(baseRealm, osMap, classContainer);
        } else {
            throw new IllegalArgumentException("Only Maps of Mixed or one of the types that can be boxed inside Mixed can be used.");
        }

        //noinspection unchecked
        return new DictionaryManager<>((MapValueOperator<V>) mapValueOperator);
    }

    private static boolean isClassBoxable(String valueClass) {
        return boxableClasses.contains(valueClass);
    }

    private static boolean isClassForRealmModel(Class<?> clazz) {
        return RealmModel.class.isAssignableFrom(clazz);
    }

    private static final List<String> boxableClasses = Arrays.asList(
            Long.class.getCanonicalName(),
            Float.class.getCanonicalName(),
            Double.class.getCanonicalName(),
            String.class.getCanonicalName(),
            Boolean.class.getCanonicalName(),
            Date.class.getCanonicalName(),
            Decimal128.class.getCanonicalName(),
            Byte[].class.getCanonicalName(),
            byte[].class.getCanonicalName(),
            ObjectId.class.getCanonicalName(),
            UUID.class.getCanonicalName()
    );
}

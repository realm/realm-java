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

import java.util.HashMap;
import java.util.Map;

import io.realm.internal.OsMap;

/**
 * Specialization for {@link RealmMap}s whose keys are strings.
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
        return getStrategy(valueClass.getCanonicalName(), baseRealm, osMap);
    }

    private static <V> ManagedMapStrategy<String, V> getStrategy(String valueClass, BaseRealm baseRealm, OsMap osMap) {
        DictionaryManagedMapOperator<V> operator = getOperator(valueClass, baseRealm, osMap);
        return new ManagedMapStrategy<>(operator);
    }

    private static <V> DictionaryManagedMapOperator<V> getOperator(String valueClass, BaseRealm baseRealm, OsMap osMap) {
        // TODO: add other types when ready
        DictionaryManagedMapOperator<V> managedMapOperator;
        if (valueClass.equals(Mixed.class.getCanonicalName())) {
            MapValueOperator<Mixed> mixedValueOperator = new MixedValueOperator(baseRealm, osMap);
            //noinspection unchecked
            managedMapOperator = new DictionaryManagedMapOperator<>((MapValueOperator<V>) mixedValueOperator);
        } else if (valueClass.equals(Boolean.class.getCanonicalName())) {
            MapValueOperator<Boolean> mixedValueOperator = new BooleanValueOperator(baseRealm, osMap);
            //noinspection unchecked
            managedMapOperator = new DictionaryManagedMapOperator<>((MapValueOperator<V>) mixedValueOperator);
        } else {
            throw new IllegalArgumentException("Only Mixed values are allowed in RealmMaps.");
        }
        return managedMapOperator;
    }
}

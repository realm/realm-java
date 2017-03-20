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
package io.realm.internal;

import java.util.IdentityHashMap;


/**
 * Identity based Set, that guarantees store & retrieve in O(1)
 * without a huge overhead in space complexity.
 */
public class IdentitySet<K> extends IdentityHashMap<K, Integer> {
    private final static Integer PLACE_HOLDER = 0;

    public void add(K key) {
        put(key, PLACE_HOLDER);
    }
}

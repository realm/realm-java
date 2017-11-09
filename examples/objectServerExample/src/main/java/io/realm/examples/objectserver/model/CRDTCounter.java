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
package io.realm.examples.objectserver.model;

import io.realm.MutableRealmInteger;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * A named, conflict-free replicated data-type.
 */
public class CRDTCounter extends RealmObject {
    @PrimaryKey
    private String name;

    @Required
    public final MutableRealmInteger counter = MutableRealmInteger.valueOf(0L);

    // Required for Realm
    public CRDTCounter() {}

    public CRDTCounter(String name) { this.name = name; }

    public String getName() { return name; }

    public long getCount() { return counter.get().longValue(); }
    public void incrementCounter(long delta) { counter.increment(delta); }
}

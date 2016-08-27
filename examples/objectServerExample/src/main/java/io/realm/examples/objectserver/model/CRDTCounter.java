/*
 * Copyright 2016 Realm Inc.
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

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Counter class that is eventually consistent. Two devices can simultaneous increment this and eventually reach
 * the same value.
 *
 * @see <href ref="https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type">Conflict Free Replicated Data Structures</href>
 */
public class CRDTCounter extends RealmObject {

    @PrimaryKey
    private long id;
    private RealmList<CounterOperation> operations;

    public CRDTCounter() {
        // Required by Realm
    }

    public CRDTCounter(long id) {
        this.id = id;
    }

    public long getCount() {
        return operations.where().sum("adjustment").longValue();
    }

    public void add(long val) {
        operations.add(new CounterOperation(val));
    }
}

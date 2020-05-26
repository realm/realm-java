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

package io.realm.mongodb.mongo.iterable;

import java.util.Collection;

import io.realm.internal.common.TaskDispatcher;
import io.realm.internal.objectstore.OsMongoFindIterable;

public class MongoFindIterable<ResultT> extends MongoIterable<ResultT> {

    private final OsMongoFindIterable<ResultT> osMongoFindIterable;

    public MongoFindIterable(final TaskDispatcher dispatcher,
                             final OsMongoFindIterable<ResultT> osMongoFindIterable) {
        super(dispatcher);
        this.osMongoFindIterable = osMongoFindIterable;
    }

    @Override
    Collection<ResultT> getCollection() {
        return osMongoFindIterable.getCollection();
    }

    @Override
    ResultT getFirst() {
        return osMongoFindIterable.first();
    }
}

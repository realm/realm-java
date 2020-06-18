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

package io.realm.mongodb.mongo.options;

import io.realm.annotations.Beta;

/**
 * The options to apply when updating documents.
 */
@Beta
public class UpdateOptions {
    private boolean upsert;

    /**
     * Returns true if a new document should be inserted if there are no matches to the query filter.
     * The default is false.
     *
     * @return true if a new document should be inserted if there are no matches to the query filter
     */
    public boolean isUpsert() {
        return upsert;
    }

    /**
     * Set to true if a new document should be inserted if there are no matches to the query filter.
     *
     * @param upsert true if a new document should be inserted if there are no matches to the query
     *               filter.
     * @return this
     */
    public UpdateOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }

    @Override
    public String toString() {
        return "RemoteUpdateOptions{"
                + "upsert=" + upsert
                + '}';
    }
}

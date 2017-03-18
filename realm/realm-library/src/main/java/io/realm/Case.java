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

/**
 * This class describes the Case settings used in Realm queries.
 *
 * @see io.realm.RealmQuery#equalTo(String, String, Case)
 * @see io.realm.RealmQuery#contains(String, String, Case)
 * @see io.realm.RealmQuery#beginsWith(String, String, Case)
 * @see io.realm.RealmQuery#endsWith(String, String, Case)
 */
public enum Case {
    SENSITIVE(true),
    INSENSITIVE(false);

    private final boolean value;

    Case(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value for this setting that is used by the underlying query engine.
     *
     * @return The value used by the underlying query engine to indicate this value.
     */
    public boolean getValue() {
        return value;
    }
}

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

package io.realm;

/**
 * Information about the changes made to an object.
 *
 * @see RealmObject#addChangeListener(RealmObjectChangeListener) .
 */
public interface ObjectChangeSet {

    /**
     * @return true if the object has been deleted from the Realm.
     */
    boolean isDeleted();

    /**
     * @return the names of changed fields if the object still exists and there are field changes. Returns an empty
     * {@code String[]} if the object has been deleted.
     */
    String[] getChangedFields();

    /**
     * Checks if a given field has been changed.
     *
     * @param fieldName to be checked if its value has been changed.
     * @return {@code true} if the field has been changed. It returns {@code false} if the object is deleted, the field
     * cannot be found or the field hasn't been changed.
     */
    boolean isFieldChanged(String fieldName);
}

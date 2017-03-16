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
 * @see RealmObject#addChangeListener(RealmObjectChangeListener) .
 */
public interface ObjectChangeSet {

    /**
     * Information about a specific field which changed in an {@link RealmObject} change notification.
     */
    class FieldChange {
        /**
         * The name of the field which changed.
         */
        final String fieldName;

        public FieldChange(String fieldName) {
            this.fieldName = fieldName;
        }
    }

    /**
     * @return {@code null} if the object has been deleted. Otherwise returns the information about changed fields.
     */
    FieldChange[] getFieldChanges();


    /**
     * @return true if the object has been deleted from the Realm.
     */
    boolean isDeleted();
}

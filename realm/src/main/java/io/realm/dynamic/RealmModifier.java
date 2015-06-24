/*
 * Copyright 2014 Realm Inc.
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

package io.realm.dynamic;

/**
 * This class contains all modifiers for a Realm field.
 * These will usually match the annotations found in the {@code io.realm.annotation} package. See the relevant
 * annotation for further information on each modifier.
 */
public enum RealmModifier {
    /**
     * Mark a field as index.
     * @see io.realm.annotations.Index
     */
    INDEXED,

    /**
     * Mark a field as a primary key.
     * @see io.realm.annotations.PrimaryKey
     */
    PRIMARY_KEY,

    /**
     * Mark a field as explicitly being able to contain {@code null} values. The default behavior for
     * allowing {@code null} depends on the type of the field.
     * @see io.realm.NonNullable
     */
    NULLABLE,

    /**
     * Mark a field as explicitly not allowing null values. The default behavior for allowing {@code
     * null} depends on the type of the field.
     * @see io.realm.NonNullable
     */
    NON_NULLABLE
}

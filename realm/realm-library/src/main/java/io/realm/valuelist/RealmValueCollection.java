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

package io.realm.valuelist;

import java.util.Collection;
import java.util.Date;

import javax.annotation.Nullable;

import io.realm.internal.ManagableObject;


public interface RealmValueCollection<E> extends Collection<E>, ManagableObject {

    /**
     * Returns a {@link RealmValueListQuery}, which can be used to query for specific objects from this collection.
     *
     * @return a RealmQuery object.
     * @throws IllegalStateException if the Realm instance has been closed or queries are not otherwise available.
     * @see io.realm.RealmQuery
     */
    RealmValueListQuery<E> where();

    /**
     * Finds the minimum value of a field.
     *
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the minimum value is returned. When determining the minimum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Number min();

    /**
     * Finds the maximum value of a field.
     *
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the maximum value is returned. When determining the maximum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Number max();

    /**
     * Calculates the sum of a given field.
     *
     * @return the sum. If no objects exist or they all have {@code null} as the value for the given field, {@code 0}
     * will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    Number sum();

    /**
     * Returns the average of a given field.
     *
     * @return the average for the given field amongst objects in query results. This will be of type double for all
     * types of number fields. If no objects exist or they all have {@code null} as the value for the given field,
     * {@code 0} will be returned. When computing the average, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    double average();

    /**
     * Finds the maximum date.
     *
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Date maxDate();

    /**
     * Finds the minimum date.
     *
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the minimum date is returned. When determining the minimum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Date minDate();

    /**
     * Checks if the collection is still valid to use, i.e., the {@link io.realm.Realm} instance hasn't been closed. It
     * will always return {@code true} for an unmanaged collection.
     *
     * @return {@code true} if it is still valid to use or an unmanaged collection, {@code false} otherwise.
     */
    @Override
    boolean isValid();

    /**
     * Checks if the collection is managed by Realm. A managed collection is just a wrapper around the data in the
     * underlying Realm file. On Looper threads, a managed collection will be live-updated so it always points to the
     * latest data. Managed collections are thread confined so that they cannot be accessed from other threads than the
     * one that created them.
     * <p>
     * <p>
     * If this method returns {@code false}, the collection is unmanaged. An unmanaged collection is just a normal java
     * collection, so it will not be live updated.
     * <p>
     *
     * @return {@code true} if this is a managed {@link RealmValueCollection}, {@code false} otherwise.
     */
    @Override
    boolean isManaged();

    /**
     * Tests whether this {@code Collection} contains the specified object. Returns
     * {@code true} if and only if at least one element {@code elem} in this
     * {@code Collection} meets following requirement:
     * {@code (object==null ? elem==null : object.equals(elem))}.
     *
     * @param object the object to search for.
     * @return {@code true} if object is an element of this {@code Collection}, {@code false} otherwise.
     * @throws NullPointerException if the object to look for is {@code null} and this {@code Collection} doesn't
     * support {@code null} elements.
     */
    @Override
    boolean contains(@Nullable Object object);
}

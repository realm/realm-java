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

package io.realm;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.annotation.Nullable;

import io.realm.internal.ManagableObject;


/**
 * {@code RealmCollection} is the root of the collection hierarchy that Realm supports. It defines operations on data
 * collections and the behavior that they will have in all implementations of {@code RealmCollection}s.
 * <p>
 * Realm collections are "live" views to the underlying data. This means that they automatically will be kept up to
 * date. As a consequence, using methods like {@link Collections#unmodifiableCollection(Collection)} will not prevent
 * a collection from being modified.
 *
 * @param <E> type of {@link RealmObject} stored in the collection.
 */
public interface RealmCollection<E> extends Collection<E>, ManagableObject {

    /**
     * Returns a {@link RealmQuery}, which can be used to query for specific objects from this collection.
     *
     * @return a RealmQuery object.
     * @throws IllegalStateException if the Realm instance has been closed or queries are not otherwise available.
     * @see io.realm.RealmQuery
     */
    RealmQuery<E> where();

    /**
     * Finds the minimum value of a field.
     *
     * @param fieldName the field to look for a minimum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the minimum value is returned. When determining the minimum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Number min(String fieldName);

    /**
     * Finds the maximum value of a field.
     *
     * @param fieldName the field to look for a maximum on. Only number fields are supported.
     * @return if no objects exist or they all have {@code null} as the value for the given field, {@code null} will be
     * returned. Otherwise the maximum value is returned. When determining the maximum value, objects with {@code null}
     * values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Number max(String fieldName);

    /**
     * Calculates the sum of a given field.
     *
     * @param fieldName the field to sum. Only number fields are supported.
     * @return the sum. If no objects exist or they all have {@code null} as the value for the given field, {@code 0}
     * will be returned. When computing the sum, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    Number sum(String fieldName);

    /**
     * Returns the average of a given field.
     *
     * @param fieldName the field to calculate average on. Only number fields are supported.
     * @return the average for the given field amongst objects in query results. This will be of type double for all
     * types of number fields. If no objects exist or they all have {@code null} as the value for the given field,
     * {@code 0} will be returned. When computing the average, objects with {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if the field is not a number type.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    double average(String fieldName);

    /**
     * Finds the maximum date.
     *
     * @param fieldName the field to look for the maximum date. If fieldName is not of Date type, an exception is
     * thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the maximum date is returned. When determining the maximum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Date maxDate(String fieldName);

    /**
     * Finds the minimum date.
     *
     * @param fieldName the field to look for the minimum date. If fieldName is not of Date type, an exception is
     * thrown.
     * @return if no objects exist or they all have {@code null} as the value for the given date field, {@code null}
     * will be returned. Otherwise the minimum date is returned. When determining the minimum date, objects with
     * {@code null} values are ignored.
     * @throws java.lang.IllegalArgumentException if fieldName is not a Date field.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    @Nullable
    Date minDate(String fieldName);

    /**
     * This deletes all objects in the collection from the underlying Realm as well as from the collection.
     *
     * @return {@code true} if objects was deleted, {@code false} otherwise.
     * @throws IllegalStateException if the corresponding Realm is closed or in an incorrect thread.
     * @throws java.lang.IllegalStateException if the Realm has been closed or called from an incorrect thread.
     */
    boolean deleteAllFromRealm();

    /**
     * Checks if a collection has finished loading its data yet.
     *
     * @return {@code true} if data has been loaded and is available, {@code false} if data is still being loaded.
     */
    boolean isLoaded();

    /**
     * Blocks the collection until all data are available.
     *
     * @return {@code true} if the data could be successfully loaded, {@code false} otherwise.
     */
    boolean load();

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
     * @return {@code true} if this is a managed {@link RealmCollection}, {@code false} otherwise.
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

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


package io.realm.rx;

import javax.annotation.Nullable;

import io.realm.ObjectChangeSet;
import io.realm.RealmModel;
import io.realm.RealmObject;

/**
 * Container wrapping the result of a {@link io.realm.RealmObjectChangeListener} being triggered.
 * <p>
 * This is used by {@link RealmObject#asChangesetObservable()} and {@link RealmObject#asChangesetObservable(RealmModel)}
 * as RxJava is only capable of emitting one item, not multiple.
 */
public class ObjectChange<E extends RealmModel> {

    private final E object;
    private final ObjectChangeSet changeset;

    /**
     * Constructor for a ObjectChange.
     *
     * @param object the object that was updated.
     * @param changeset the changeset describing the update.
     */
    public ObjectChange(E object, @Nullable ObjectChangeSet changeset) {
        this.object = object;
        this.changeset = changeset;
    }

    public E getObject() {
        return object;
    }

    /**
     * Returns the changeset describing the update.
     * <p>
     * This will be {@code null} the first time the stream emits the object as well as when a asynchronous query
     * is loaded for the first time.
     * <p>
     * <pre>
     * {@code
     * // Example
     * realm.where(Person.class).findFirstAsync().asChangesetObservable()
     *   .subscribe(new Consumer<ObjectChange>() {
     *    \@Override
     *     public void accept(ObjectChange item) throws Exception {
     *       item.getChangeset(); // Will return null the first two times
     *   }
     * });
     * }
     * </pre>
     *
     * @return the changeset describing how the object was updated.
     */
    @Nullable
    public ObjectChangeSet getChangeset() {
        return changeset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectChange<?> that = (ObjectChange<?>) o;

        if (!object.equals(that.object)) return false;
        return changeset != null ? changeset.equals(that.changeset) : that.changeset == null;
    }

    @Override
    public int hashCode() {
        int result = object.hashCode();
        result = 31 * result + (changeset != null ? changeset.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ObjectChange{" +
                "object=" + object +
                ", changeset=" + changeset +
                '}';
    }
}
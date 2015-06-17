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

package io.realm;

import io.realm.annotations.RealmClass;
import io.realm.internal.Row;
import io.realm.internal.InvalidRow;

/**
 * In Realm you define your model classes by sub-classing RealmObject and adding fields to be
 * persisted. You then create your objects within a Realm, and use your custom subclasses instead
 * of using the RealmObject class directly.
 * <p>
 * An annotation processor will create a proxy class for your RealmObject subclass. The getters and
 * setters should not contain any custom code of logic as they are overridden as part of the annotation
 * process.
 * <p>
 * A RealmObject is currently limited to the following:
 *
 * <ul>
 *   <li>Private fields.</li>
 *   <li>Getter and setters for these fields.</li>
 *   <li>Static methods.</li>
 * </ul>
 * <p>
 * The following field data types are supported (no boxed types):
 * <ul>
 *   <li>boolean</li>
 *   <li>short</li>
 *   <li>int</li>
 *   <li>long</li>
 *   <li>float</li>
 *   <li>double</li>
 *   <li>byte[]</li>
 *   <li>String</li>
 *   <li>Date</li>
 *   <li>Any RealmObject subclass</li>
 *   <li>RealmList</li>
 * </ul>
 * <p>
 * The types <code>short</code>, <code>int</code>, and <code>long</code> are mapped to <code>long</code>
 * when storing within a Realm.
 * <p>
 * Getter and setter names must have the name {@code getXXX} or {@code setXXX} if
 * the field name is {@code XXX}. Getters for fields of type boolean can be called {@code isXXX} as
 * well. Fields with a m-prefix must have getters and setters named setmXXX and getmXXX which is
 * the default behavior when Android Studio automatically generates the getters and setters.
 * <p>
 * Fields annotated with {@link io.realm.annotations.Ignore} don't have these restrictions and
 * don't require either a getter or setter.
 * <p>
 * Realm will create indexes for fields annotated with {@link io.realm.annotations.Index}. This
 * will speedup queries but will have a negative impact on inserts and updates.
 * * <p>
 * A RealmObject cannot be passed between different threads.
 *
 * @see Realm#createObject(Class)
 * @see Realm#copyToRealm(RealmObject)
 */

@RealmClass
public abstract class RealmObject {

    protected Row row;
    protected Realm realm;

    /**
     * Removes the object from the Realm it is currently associated to.
     * <p>
     * After this method is called the object will be invalid and any operation (read or write)
     * performed on it will fail with an IllegalStateException
     */
    public void removeFromRealm() {
        if (row == null) {
            throw new IllegalStateException("Object malformed: missing object in Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        if (realm == null) {
            throw new IllegalStateException("Object malformed: missing Realm. Make sure to instantiate RealmObjects with Realm.createObject()");
        }
        row.getTable().moveLastOver(row.getIndex());
        row = new InvalidRow();
    }

    /**
     * Check if the RealmObject is still valid to use ie. the RealmObject hasn't been deleted nor
     * has the {@link io.realm.Realm} been closed. It will always return false for stand alone
     * objects.
     *
     * @return {@code true} if the object is still accessible, {@code false} otherwise or if it is a
     * standalone object.
     */
    public boolean isValid() {
        return row != null && row.isAttached();
    }
}

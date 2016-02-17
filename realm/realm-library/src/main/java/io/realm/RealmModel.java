package io.realm;

import io.realm.annotations.RealmClass;


/**
 * Interface for marking classes as RealmObjects, it can be used instead of extending {@link RealmObject}.
 *
 * All helper methods available to classes that extend RealmObject are instead available as static methods:
 *
 * <pre>
 * {@code
 *   Person p = realm.createObject(Person.class);
 *
 *   // With the RealmModel interface
 *   RealmObject.isValid(p);
 *
 *   // With the RealmObject base class
 *   p.isValid();
 * }
 * </pre>
 *
 * @see RealmObject
 */

@RealmClass
public interface RealmModel {
}

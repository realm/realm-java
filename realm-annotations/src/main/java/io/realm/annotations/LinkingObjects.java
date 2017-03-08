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

package io.realm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a reverse relationship from one class to another. This annotation can
 * only be added to a field of the type {@code RealmResults}.
 *<pre>
 * To expose reverse relationships for use, create a declaration as follows:
 * {@code
 *
 * public Class Person extends RealmObject {
 *   private String name;
 *   private Dog dog; // Normal reference
 * }
 *
 * public Class Dog extends RealmObject {
 *   // This holds all Person objects with a reference to this Dog object (= linking objects)
 *   \@LinkingObjects("dog")
 *   private final RealmResults&gt;Person&lt; owners = null;
 * }
 *
 * // Find all Dogs with at least one owner named John
 * realm.where(Dog.class).equalTo("owners.name", "John").findAll();
 * }
 * </pre>
 * In the above example `Person` holds a reference to `Dog` through the field `dog`.
 * This in turn means that an implict reverse relationship exists between the class `Dog`
 * and the class `Person`. This inverse relationship is made public and queryable by the `RealmResults`
 * field annotated with `@LinkingObject`. This makes it possible to query properties of the dogs owner
 * without having to manually maintain a "owner" field in the `Dog` class.
 * <p>
 * Linking objects have the following properties:
 * <ul>
 *     <li>The link is maintained by Realm and only works for managed objects.</li>
 *     <li>They can be queried just like normal references.</li>
 *     <li>They can be followed just like normal references.</li>
 *     <li>They are ignored when doing a `copyToRealm().`</li>
 *     <li>They are ignored when doing a `copyFromRealm().`</li>
 *     <li>They are ignored when using the various `createObjectFromJson*` and `createAllFromJson*` methods.</li>
 * </ul>
 * <p>
 * In addition, they have the following restrictions:
 * <ul>
 *     <li>{@literal @}Ignore takes precedence.  A {@literal @}LinkingObjects annotation on {@literal @}Ignore field will be ignored.</li>
 *     <li>The annotated field cannot be {@literal @}Required.</li>
 *     <li>The annotated field must be `final`.</li>
 *     <li>The annotation argument (the name of the backlinked field) is required.</li>
 *     <li>The annotation argument must be a simple field name.  It cannot containt periods ('.').</li>
 *     <li>The annotation field must be of type `RealmResults&gt;T&lt;` where T is concrete class that extends `RealmModel`.</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface LinkingObjects {
    /**
     * The name of a field that contains a reference to an instance of the
     * class containing this annotation.  If this argument is not provided
     * the annotation processor will abort with an {@code IllegalArgumentException}.
     */
    String value() default "";
}

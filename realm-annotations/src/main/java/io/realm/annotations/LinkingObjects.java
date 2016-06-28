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

package io.realm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining a reverse relationship from one class to another. This annotation can
 * only be added to a field of the type {@code RealmResults}.
 *
 * Example:
 * <pre>
 * {@code
 *
 * public Class Person extends RealmObject {
 *   private String name;
 *   private Dog dog; // Normal reference
 * }
 *
 * public Class Dog extends RealmObject {
 *   // This hold all Person objects with a reference to this Dog object (= linking objects)
 *   \@LinkingObjects("dog")
 *   private RealmResults&gt;Person&lt; owners;
 * }
 *
 * // Find all Dogs with at least one owner named John
 * realm.where(Dog.class).equalTo("owners.name", "John").findAll();
 * }
 * </pre>
 *
 * Linking objects have the following properties:
 * <ul>
 *     <li>The link is maintained by Realm and only work on managed objects.</li>
 *     <li>They can be queried just like normal references.</li>
 *     <li>They can be followed just like normal references.</li>
 *     <li>They will be ignored when doing a `copyToRealm()`</li>
 *     <li>They will be ignored when doing a `copyFromRealm()`</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface LinkingObjects {
    /**
     * The field in the Class that is referencing this class. If not provided the annotation
     * processor will crash with an {@code IllegalArgumentException}.
     */
    String value() default "";
}

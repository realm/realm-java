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

package io.realm.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to map automatically between the name defined in Java and the names used by
 * Realm's internal storage engine. The annotation can be applied to any Realm model or module class as
 * well as any fields in a model class.
 * <p>
 * Examples where this can be useful is e.g:
 * <ul>
 *      <li>
 *          To support two model classes with the same name in different packages.
 *      </li>
 *      <li>
 *          To make it easier to work with cross platform schemas as naming conventions are different.
 *      </li>
 *      <li>
 *          To bring a class name below the normal 57 character limit.
 *      </li>
 * </ul>
 * Example:
 * <pre>
 * {@code
 * \@RealmName(name = "person")
 * public class Person extends RealmObject {
 *   \@RealmName(name = "first_name")
 *   public name firstName;
 * }
 *
 * \@RealmName(policy = RealmNamePolicy.LOWER_CASE_WITH_UNDERSCORES)
 * public class Person extends RealmObject { // is converted to "person"
 *     public string firstName; // Is converted to "first_name"
 * }
 * }
 * </pre>
 * <p>
 * The semantics of this annotation change slightly depending on where it is applied:
 * <ul>
 *     <li>
 *         If applied to a module, any defined {@code policy} will effect all classes and fields
 *         part of that module. An error is thrown if a Realm model class is part of two modules
 *         that do no not have the same name policy. Using {@code name} is not
 *         applicable for modules, so an error will be thrown if used. {@code recursive} should
 *         always be {@code true} if used on an module.
 *
 *         FIXME: Need test setting name
 *         FIXME: Need check for class being part of two modules with different policies
 *     </li>
 *      <li>
 *          If applied to a class, any defined {@code policy} will effect the class name and all
 *          fields in that class. This will override any policy specified on a module.
 *          Set {@code recursive == false} to only let the policy apply to the class name and
 *          not the fields inside the class.
 *
 *          FIXME: Need test for using recursive
 *      </li>
 *      <li>
 *          If applied to a field setting, any policy will only apply to that field. Setting
 *          {code recursive()} has no effect.
 *      </li>
 * </ul>
 * <p>
 * Changing the internal name has the following implications:
 * <ul>
 *      <li>
 *          Queries on {@code DynamicRealm} must use the internal name. Queries on normal {@code Realm}
 *          instances should continue to use the name as it is defined in the Java class.
 *      </li>
 *      <li>
 *          Migrations must use the internal name when creating classes and fields.
 *      </li>
 *      <li>
 *          Interacting with the Realm schema using {@code RealmSchema} or {@code RealmObjectSchema} should
 *          be done using the internal name.
 *      </li>
 * </ul>
 * <p>
 * Note, that changing the internal name does not effect importing data from JSON. The JSON data
 * must still follow the names as defined in the Realm Java class.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RealmName {
    /**
     * Set the name specifically for this field or class. This will override any defined {@code policy}.
     * Setting the name only applies on a Realm model class or field controlled by Realm.
     */
    String name() default "";

    /**
     * Sets the policy for automatically converting the name used in the Java class.
     * The default is to use the name as specified in the Java model class.
     *
     * @see RealmNamePolicy
     */
    RealmNamePolicy policy() default RealmNamePolicy.NO_POLICY;

    /**
     * If {@code true}, the defined {@link RealmNamePolicy} will recursively be applied to underlying
     * elements. This setting only applies if set for a class or a module. The default value is {@code true}.
     * @return
     */
    boolean recursive() default true;
}

/*
 * Copyright 2015 Realm Inc.
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
 * This annotation will mark the field or the element in {@code io.realm.RealmList} as not nullable.
 * <p>
 * When a field of type {@code Boolean, Byte, Short, Integer, Long, Float, Double, String, byte[], Date} is annotated
 * with {@link Required}, it cannot be set to {@code null} and Realm will throw an exception if it happens.
 * <p>
 * Fields with primitive types are implicitly required. Note, {@code String} is not a primitive type, so in Java
 * it is default nullable unless it is marked {@code \@Required}. In Kotlin the reverse is true, so a {@code String} is
 * non-null. To specify a nullable String in Kotlin you should use {@code String?}.
 * <p>
 * If this annotation is used on a {@code RealmList}, the annotation is applied to the elements inside
 * the list and not the list itself. The list itself is always non-null. This means that a list marked with this
 * annotation are never allowed to hold {@code null} values even if the datatype would otherwise allow it.
 * Realm will throw an exception if you attempt to store null values into a list marked {@code \@Required}.
 * <p>
 * This annotation cannot be used on a {@code RealmAny}, as the inner value of a RealmAny field is always nullable.
 * Realm will throw an exception if you attempt mark a {@code RealmAny} as {@code \@Required}.
 * <p>
 * Compiling will fail if the {@link Required} annotation is put an a {@code RealmList} containing references to other
 * Realm objects.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Required {

}

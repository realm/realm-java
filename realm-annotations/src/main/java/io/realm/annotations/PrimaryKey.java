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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @PrimaryKey annotation will mark a field as a primary key inside Realm. Only one field in a
 * RealmObject class can have this annotation, and the field should uniquely identify the object.
 * Trying to insert an object with an existing primary key will result in an
 * {@link io.realm.exceptions.RealmPrimaryKeyConstraintException}. Primary key cannot be changed
 * after the object created.
 * <p>
 * Primary keys also count as having the {@link Index} annotation.
 * <p>
 * It is allowed to apply this annotation on the following primitive types: byte, short, int, and long.
 * String, Byte, Short, Integer, and Long are also allowed, and further permitted to have {@code null}
 * as a primary key value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

}
